package spms;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ScheduleEngine {
    private ScheduleEngine() {}

    public static ScheduleResult generateProjectSchedule(Connection c, int projectId) throws SQLException {
        LocalDate projectStart = null;
        LocalDate projectDeadline = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT start_date, deadline FROM project WHERE id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectStart = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                    projectDeadline = rs.getDate("deadline") == null ? null : rs.getDate("deadline").toLocalDate();
                }
            }
        }

        if (projectStart == null) {
            throw new SQLException("Project start date is missing.");
        }

        Map<Integer, ActivityRecord> activities = new HashMap<>();
        Map<Integer, List<Integer>> predecessors = new HashMap<>();
        Map<Integer, List<Integer>> successors = new HashMap<>();

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, code, name, duration_days, start_date, end_date, baseline_start, baseline_end " +
                        "FROM activity WHERE project_id = ? ORDER BY id")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    ActivityRecord record = new ActivityRecord();
                    record.id = id;
                    record.code = rs.getString("code");
                    record.name = rs.getString("name");
                    record.durationDays = rs.getInt("duration_days");
                    record.startDate = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                    record.endDate = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
                    record.baselineStart = rs.getDate("baseline_start") == null ? null : rs.getDate("baseline_start").toLocalDate();
                    record.baselineEnd = rs.getDate("baseline_end") == null ? null : rs.getDate("baseline_end").toLocalDate();
                    activities.put(id, record);
                    predecessors.put(id, new ArrayList<>());
                    successors.put(id, new ArrayList<>());
                }
            }
        }

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT activity_id, predecessor_id FROM activity_dependency WHERE activity_id IN (SELECT id FROM activity WHERE project_id = ?)")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int activityId = rs.getInt("activity_id");
                    int predecessorId = rs.getInt("predecessor_id");
                    predecessors.get(activityId).add(predecessorId);
                    successors.computeIfAbsent(predecessorId, k -> new ArrayList<>()).add(activityId);
                }
            }
        }

        Map<Integer, LocalDate> newStartDates = new HashMap<>();
        Map<Integer, LocalDate> newEndDates = new HashMap<>();

        List<Integer> order = topologicalSort(predecessors, successors);
        for (int id : order) {
            ActivityRecord record = activities.get(id);
            if (record == null) continue;
            LocalDate start = projectStart;
            if (!predecessors.get(id).isEmpty()) {
                LocalDate latestPredEnd = null;
                for (int predId : predecessors.get(id)) {
                    LocalDate predEnd = newEndDates.get(predId);
                    if (predEnd != null && (latestPredEnd == null || predEnd.isAfter(latestPredEnd))) {
                        latestPredEnd = predEnd;
                    }
                }
                if (latestPredEnd != null) {
                    start = latestPredEnd.plusDays(1);
                }
            }
            LocalDate end = start.plusDays(record.durationDays - 1);
            newStartDates.put(id, start);
            newEndDates.put(id, end);
        }

        try (PreparedStatement ps = c.prepareStatement("UPDATE activity SET start_date = ?, end_date = ? WHERE id = ? AND project_id = ?")) {
            for (Map.Entry<Integer, ActivityRecord> entry : activities.entrySet()) {
                int id = entry.getKey();
                LocalDate start = newStartDates.get(id);
                LocalDate end = newEndDates.get(id);
                if (start == null || end == null) continue;
                ps.setDate(1, Date.valueOf(start));
                ps.setDate(2, Date.valueOf(end));
                ps.setInt(3, id);
                ps.setInt(4, projectId);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        LocalDate projectFinish = Collections.max(newEndDates.values());
        boolean overdue = projectDeadline != null && projectFinish.isAfter(projectDeadline);
        return new ScheduleResult(projectFinish, projectDeadline, overdue, newStartDates, newEndDates);
    }

    private static List<Integer> topologicalSort(Map<Integer, List<Integer>> predecessors, Map<Integer, List<Integer>> successors) {
        List<Integer> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> visiting = new HashSet<>();

        List<Integer> nodes = new ArrayList<>(predecessors.keySet());
        Collections.sort(nodes);
        for (int node : nodes) {
            dfs(node, successors, visited, visiting, result);
        }
        return result;
    }

    private static void dfs(int node, Map<Integer, List<Integer>> successors, Set<Integer> visited, Set<Integer> visiting, List<Integer> order) {
        if (visited.contains(node)) {
            return;
        }
        if (visiting.contains(node)) {
            return;
        }
        visiting.add(node);
        for (int next : successors.getOrDefault(node, Collections.emptyList())) {
            dfs(next, successors, visited, visiting, order);
        }
        visiting.remove(node);
        visited.add(node);
        order.add(node);
    }

    public static String buildDelayImpactText(Connection c, int projectId) throws SQLException {
        LocalDate statusDate = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT status_date FROM project WHERE id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    statusDate = rs.getDate("status_date") == null ? null : rs.getDate("status_date").toLocalDate();
                }
            }
        }

        if (statusDate == null) {
            return "Status date is not set.";
        }

        Map<Integer, ActivityRecord> activities = new HashMap<>();
        Map<Integer, List<Integer>> successors = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT a.id, a.code, a.name, a.status, a.end_date, a.baseline_end, a.start_date, a.duration_days " +
                        "FROM activity a WHERE a.project_id = ? ORDER BY a.id")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityRecord record = new ActivityRecord();
                    record.id = rs.getInt("id");
                    record.code = rs.getString("code");
                    record.name = rs.getString("name");
                    record.status = rs.getString("status");
                    record.endDate = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
                    record.baselineEnd = rs.getDate("baseline_end") == null ? null : rs.getDate("baseline_end").toLocalDate();
                    record.startDate = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                    record.durationDays = rs.getInt("duration_days");
                    activities.put(record.id, record);
                    successors.put(record.id, new ArrayList<>());
                }
            }
        }

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT activity_id, predecessor_id FROM activity_dependency WHERE activity_id IN (SELECT id FROM activity WHERE project_id = ?)")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int activityId = rs.getInt("activity_id");
                    int predecessorId = rs.getInt("predecessor_id");
                    successors.computeIfAbsent(predecessorId, k -> new ArrayList<>()).add(activityId);
                }
            }
        }

        Set<Integer> delayed = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (ActivityRecord rec : activities.values()) {
            if (!"Completed".equals(rec.status) && rec.endDate != null) {
                boolean isDelayed = rec.endDate.isBefore(statusDate) || (rec.baselineEnd != null && rec.endDate.isAfter(rec.baselineEnd));
                if (isDelayed) {
                    delayed.add(rec.id);
                    int delayDays = 0;
                    if (rec.endDate.isBefore(statusDate)) {
                        delayDays = (int) java.time.temporal.ChronoUnit.DAYS.between(rec.endDate, statusDate);
                    }
                    if (rec.baselineEnd != null && rec.endDate.isAfter(rec.baselineEnd)) {
                        delayDays = Math.max(delayDays, (int) java.time.temporal.ChronoUnit.DAYS.between(rec.baselineEnd, rec.endDate));
                    }
                    sb.append(rec.code).append(" delayed by ").append(delayDays).append(" days.\n");
                }
            }
        }

        Set<Integer> impactSet = new HashSet<>();
        for (int delayedId : delayed) {
            collectDependents(delayedId, successors, impactSet);
        }

        if (impactSet.isEmpty()) {
            if (sb.length() == 0) {
                return "No delayed activities found at the current status date.";
            }
            return sb.toString();
        }

        sb.append("\nDependent activities impacted:\n");
        for (int id : impactSet) {
            ActivityRecord rec = activities.get(id);
            if (rec != null) {
                sb.append(rec.code).append(" - ").append(rec.name).append("\n");
            }
        }

        LocalDate projectFinish = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT MAX(end_date) FROM activity WHERE project_id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getDate(1) != null) {
                    projectFinish = rs.getDate(1).toLocalDate();
                }
            }
        }

        LocalDate deadline = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT deadline FROM project WHERE id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    deadline = rs.getDate("deadline") == null ? null : rs.getDate("deadline").toLocalDate();
                }
            }
        }

        if (projectFinish != null && deadline != null) {
            long lateDays = java.time.temporal.ChronoUnit.DAYS.between(deadline, projectFinish);
            if (lateDays > 0) {
                sb.append("\nFinal project deadline impact: ").append(lateDays).append(" days late.\n");
            }
        }

        return sb.toString();
    }

    private static void collectDependents(int activityId, Map<Integer, List<Integer>> successors, Set<Integer> result) {
        for (int child : successors.getOrDefault(activityId, Collections.emptyList())) {
            if (result.add(child)) {
                collectDependents(child, successors, result);
            }
        }
    }

    public static List<String> resourceSummary(Connection c, int projectId) throws SQLException {
        Map<Integer, String> memberNames = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM team_member WHERE project_id = ? ORDER BY name")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    memberNames.put(rs.getInt("id"), rs.getString("name"));
                }
            }
        }

        Map<Integer, List<ActivityRecord>> assigned = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT a.id, a.code, a.name, a.assignee_id, a.start_date, a.end_date, a.duration_days FROM activity a WHERE a.project_id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int assigneeId = rs.getInt("assignee_id");
                    if (rs.wasNull()) continue;
                    ActivityRecord rec = new ActivityRecord();
                    rec.id = rs.getInt("id");
                    rec.code = rs.getString("code");
                    rec.name = rs.getString("name");
                    rec.startDate = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                    rec.endDate = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
                    rec.durationDays = rs.getInt("duration_days");
                    assigned.computeIfAbsent(assigneeId, k -> new ArrayList<>()).add(rec);
                }
            }
        }

        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, List<ActivityRecord>> entry : assigned.entrySet()) {
            int memberId = entry.getKey();
            String name = memberNames.get(memberId);
            if (name == null) continue;
            int totalDays = 0;
            StringBuilder activityList = new StringBuilder();
            for (ActivityRecord rec : entry.getValue()) {
                totalDays += rec.durationDays;
                activityList.append(rec.code).append(" (" + rec.startDate + " - " + rec.endDate + ") ; ");
            }
            result.add(name + " | total days=" + totalDays + " | activities=" + activityList);
        }
        return result;
    }

    public static boolean hasResourceOverlap(Connection c, int projectId) throws SQLException {
        List<ActivityRecord> records = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, code, name, assignee_id, start_date, end_date FROM activity WHERE project_id = ? AND assignee_id IS NOT NULL")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityRecord rec = new ActivityRecord();
                    rec.id = rs.getInt("id");
                    rec.code = rs.getString("code");
                    rec.name = rs.getString("name");
                    rec.assigneeId = rs.getInt("assignee_id");
                    rec.startDate = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                    rec.endDate = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
                    records.add(rec);
                }
            }
        }

        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                ActivityRecord a = records.get(i);
                ActivityRecord b = records.get(j);
                if (a.assigneeId != b.assigneeId) continue;
                if (a.startDate == null || a.endDate == null || b.startDate == null || b.endDate == null) continue;
                if (!(a.startDate.isAfter(b.endDate) || b.startDate.isAfter(a.endDate))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final class ScheduleResult {
        public final LocalDate projectFinishDate;
        public final LocalDate projectDeadline;
        public final boolean overDeadline;
        public final Map<Integer, LocalDate> startDates;
        public final Map<Integer, LocalDate> endDates;

        public ScheduleResult(LocalDate projectFinishDate, LocalDate projectDeadline, boolean overDeadline,
                             Map<Integer, LocalDate> startDates, Map<Integer, LocalDate> endDates) {
            this.projectFinishDate = projectFinishDate;
            this.projectDeadline = projectDeadline;
            this.overDeadline = overDeadline;
            this.startDates = startDates;
            this.endDates = endDates;
        }
    }

    private static final class ActivityRecord {
        int id;
        String code;
        String name;
        String status;
        int durationDays;
        Integer assigneeId;
        LocalDate startDate;
        LocalDate endDate;
        LocalDate baselineStart;
        LocalDate baselineEnd;
    }
}
