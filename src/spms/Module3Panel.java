package spms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Module3Panel extends JPanel {
    private static final int PROJECT_ID = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DefaultTableModel activityModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Name", "WBS", "Assignee", "Requirement", "Duration", "Start", "End", "Baseline Start", "Baseline End", "%", "Planned Cost", "Actual Cost", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable activityTable = new JTable(activityModel);

    private final DefaultTableModel resourceModel = new DefaultTableModel(
            new Object[]{"Member", "Activities", "Total Days", "Overlap"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable resourceTable = new JTable(resourceModel);

    private final JComboBox<String> dependencyActivityCombo = new JComboBox<>();
    private final JList<String> predecessorList = new JList<>();
    private final JTextArea delayImpactArea = new JTextArea(10, 45);
    private final JPanel ganttPanel = new GanttPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public Module3Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildActivitySection(),
                buildLowerSection());
        mainSplit.setResizeWeight(0.72);
        add(mainSplit, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadAll();
    }

    private JPanel buildActivitySection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Activities"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton generateBtn = new JButton("Generate Schedule");
        JButton depsBtn = new JButton("Save Dependencies");

        addBtn.addActionListener(e -> promptActivityDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedActivity());
        deleteBtn.addActionListener(e -> deleteSelectedActivity());
        refreshBtn.addActionListener(e -> loadAll());
        generateBtn.addActionListener(e -> generateSchedule());
        depsBtn.addActionListener(e -> saveSelectedDependencies());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(generateBtn);
        buttons.add(depsBtn);
        buttons.add(refreshBtn);

        panel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildLowerSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createTitledBorder("Dependencies"));
        JPanel dependencyControls = new JPanel(new GridLayout(2, 1, 5, 5));
        dependencyControls.add(new JLabel("Selected activity:"));
        dependencyControls.add(dependencyActivityCombo);
        dependencyActivityCombo.addActionListener(e -> refreshDependencySelection());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        predecessorList.setModel(listModel);
        predecessorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        left.add(dependencyControls, BorderLayout.NORTH);
        left.add(new JScrollPane(predecessorList), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createTitledBorder("Delay Impact"));
        delayImpactArea.setEditable(false);
        delayImpactArea.setLineWrap(true);
        delayImpactArea.setWrapStyleWord(true);
        right.add(new JScrollPane(delayImpactArea), BorderLayout.CENTER);

        JSplitPane midSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        midSplit.setResizeWeight(0.5);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBorder(BorderFactory.createTitledBorder("Resource View"));
        bottom.add(new JScrollPane(resourceTable), BorderLayout.CENTER);

        JPanel schedulePanel = new JPanel(new BorderLayout());
        schedulePanel.setBorder(BorderFactory.createTitledBorder("Schedule Gantt"));
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        schedulePanel.add(ganttScroll, BorderLayout.CENTER);

        JSplitPane secondSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, midSplit, bottom);
        secondSplit.setResizeWeight(0.62);

        JPanel total = new JPanel(new BorderLayout(8, 8));
        total.add(secondSplit, BorderLayout.CENTER);
        total.add(schedulePanel, BorderLayout.SOUTH);
        panel.add(total, BorderLayout.CENTER);
        return panel;
    }

    private void loadAll() {
        loadActivities();
        refreshDependencySelection();
        loadResourceView();
        renderDelayImpact();
        renderGantt();
    }

    private void loadActivities() {
        activityModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT a.id, a.code, a.name, w.code AS wbs_code, w.name AS wbs_name, tm.name AS assignee_name, r.code AS req_code, a.duration_days, a.start_date, a.end_date, a.baseline_start, a.baseline_end, a.percent_complete, a.planned_cost, a.actual_cost, a.status " +
                             "FROM activity a " +
                             "LEFT JOIN wbs_item w ON w.id = a.wbs_id AND w.project_id = ? " +
                             "LEFT JOIN team_member tm ON tm.id = a.assignee_id AND tm.project_id = ? " +
                             "LEFT JOIN requirement r ON r.id = a.requirement_id AND r.project_id = ? " +
                             "WHERE a.project_id = ? ORDER BY a.id")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, PROJECT_ID);
            ps.setInt(3, PROJECT_ID);
            ps.setInt(4, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activityModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("wbs_code") == null ? "" : rs.getString("wbs_code") + " - " + (rs.getString("wbs_name") == null ? "" : rs.getString("wbs_name")),
                            rs.getString("assignee_name") == null ? "" : rs.getString("assignee_name"),
                            rs.getString("req_code") == null ? "" : rs.getString("req_code"),
                            rs.getInt("duration_days"),
                            rs.getDate("start_date") == null ? "" : rs.getDate("start_date").toLocalDate().format(DATE_FORMAT),
                            rs.getDate("end_date") == null ? "" : rs.getDate("end_date").toLocalDate().format(DATE_FORMAT),
                            rs.getDate("baseline_start") == null ? "" : rs.getDate("baseline_start").toLocalDate().format(DATE_FORMAT),
                            rs.getDate("baseline_end") == null ? "" : rs.getDate("baseline_end").toLocalDate().format(DATE_FORMAT),
                            rs.getInt("percent_complete"),
                            rs.getBigDecimal("planned_cost"),
                            rs.getBigDecimal("actual_cost"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load activities. " + ex.getMessage());
        }

        dependencyActivityCombo.removeAllItems();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, name FROM activity WHERE project_id = ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dependencyActivityCombo.addItem(rs.getString("code") + " - " + rs.getString("name"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load dependency selector. " + ex.getMessage());
        }
    }

    private void refreshDependencySelection() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, name FROM activity WHERE project_id = ? AND id <> ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            String selectedLabel = (String) dependencyActivityCombo.getSelectedItem();
            int currentId = selectedLabel == null ? -1 : activityIdFromLabel(selectedLabel);
            ps.setInt(2, currentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listModel.addElement(rs.getString("code") + " - " + rs.getString("name"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load dependency candidates. " + ex.getMessage());
            return;
        }

        predecessorList.setModel(listModel);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT a.code, a.name FROM activity_dependency ad JOIN activity a ON a.id = ad.predecessor_id WHERE ad.activity_id = ?")) {
            String selectedLabel = (String) dependencyActivityCombo.getSelectedItem();
            int currentId = selectedLabel == null ? -1 : activityIdFromLabel(selectedLabel);
            if (currentId < 0) {
                predecessorList.clearSelection();
                return;
            }
            ps.setInt(1, currentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> selectedIndices = new ArrayList<>();
                for (int i = 0; i < listModel.size(); i++) {
                    String option = listModel.get(i);
                    String code = option.split(" - ")[0];
                    boolean found = false;
                    while (rs.next()) {
                        String candidate = rs.getString("code") + " - " + rs.getString("name");
                        if (candidate.equals(option)) {
                            found = true;
                            break;
                        }
                    }
                    rs.beforeFirst();
                    if (found) {
                        selectedIndices.add(i);
                    }
                }
                for (int idx : selectedIndices) {
                    predecessorList.addSelectionInterval(idx, idx);
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load selected predecessors. " + ex.getMessage());
        }
    }

    private void saveSelectedDependencies() {
        String selectedLabel = (String) dependencyActivityCombo.getSelectedItem();
        if (selectedLabel == null) {
            showError("Select an activity first.");
            return;
        }
        int activityId = activityIdFromLabel(selectedLabel);
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement deletePs = c.prepareStatement("DELETE FROM activity_dependency WHERE activity_id = ?")) {
                deletePs.setInt(1, activityId);
                deletePs.executeUpdate();
            }

            int[] selectedIndices = predecessorList.getSelectedIndices();
            for (int idx : selectedIndices) {
                String option = predecessorList.getModel().getElementAt(idx);
                int predId = activityIdFromLabel(option);
                if (predId == activityId) {
                    c.rollback();
                    showError("Self-dependency is not allowed.");
                    return;
                }
                try (PreparedStatement insertPs = c.prepareStatement("INSERT INTO activity_dependency (activity_id, predecessor_id) VALUES (?, ?)")) {
                    insertPs.setInt(1, activityId);
                    insertPs.setInt(2, predId);
                    insertPs.executeUpdate();
                }
            }

            if (hasCircularDependency(c, activityId)) {
                c.rollback();
                showError("Circular dependency detected. Please remove the cycle before saving.");
                return;
            }
            c.commit();
            statusLabel.setText("Dependencies updated.");
            renderDelayImpact();
        } catch (SQLException ex) {
            showError("Unable to save dependencies. " + ex.getMessage());
        }
    }

    private boolean hasCircularDependency(Connection c, int activityId) throws SQLException {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT activity_id, predecessor_id FROM activity_dependency WHERE activity_id IN (SELECT id FROM activity WHERE project_id = ?)")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int a = rs.getInt("activity_id");
                    int p = rs.getInt("predecessor_id");
                    graph.computeIfAbsent(a, k -> new ArrayList<>()).add(p);
                }
            }
        }
        return hasCycle(graph, activityId, new ArrayList<>());
    }

    private boolean hasCycle(Map<Integer, List<Integer>> graph, int current, List<Integer> stack) {
        if (stack.contains(current)) {
            return true;
        }
        stack.add(current);
        for (int next : graph.getOrDefault(current, new ArrayList<>())) {
            if (hasCycle(graph, next, new ArrayList<>(stack))) {
                return true;
            }
        }
        return false;
    }

    private void promptActivityDialog(boolean edit, int selectedId) {
        String currentCode = "";
        String currentName = "";
        int currentWbsId = -1;
        int currentAssigneeId = -1;
        int currentRequirementId = -1;
        int currentDuration = 1;
        String currentStartDate = "";
        String currentEndDate = "";
        String currentBaselineStart = "";
        String currentBaselineEnd = "";
        int currentPercent = 0;
        BigDecimal currentPlannedCost = BigDecimal.ZERO;
        BigDecimal currentActualCost = BigDecimal.ZERO;
        String currentStatus = "Not Started";

        if (edit) {
            int row = activityTable.getSelectedRow();
            if (row < 0) {
                showError("Select an activity first.");
                return;
            }
            selectedId = Integer.parseInt(activityModel.getValueAt(row, 0).toString());
            currentCode = activityModel.getValueAt(row, 1).toString();
            currentName = activityModel.getValueAt(row, 2).toString();
            currentDuration = Integer.parseInt(activityModel.getValueAt(row, 6).toString());
            currentStartDate = activityModel.getValueAt(row, 7) == null ? "" : activityModel.getValueAt(row, 7).toString();
            currentEndDate = activityModel.getValueAt(row, 8) == null ? "" : activityModel.getValueAt(row, 8).toString();
            currentBaselineStart = activityModel.getValueAt(row, 9) == null ? "" : activityModel.getValueAt(row, 9).toString();
            currentBaselineEnd = activityModel.getValueAt(row, 10) == null ? "" : activityModel.getValueAt(row, 10).toString();
            currentPercent = Integer.parseInt(activityModel.getValueAt(row, 11).toString());
            currentPlannedCost = new BigDecimal(activityModel.getValueAt(row, 12).toString());
            currentActualCost = new BigDecimal(activityModel.getValueAt(row, 13).toString());
            currentStatus = activityModel.getValueAt(row, 14).toString();

            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement("SELECT wbs_id, assignee_id, requirement_id FROM activity WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, selectedId);
                ps.setInt(2, PROJECT_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentWbsId = rs.getInt("wbs_id");
                        currentAssigneeId = rs.getInt("assignee_id");
                        currentRequirementId = rs.getInt("requirement_id");
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load selected activity details. " + ex.getMessage());
                return;
            }
        }

        Map<String, Integer> wbsMap = loadWbsMap();
        Map<String, Integer> memberMap = loadMemberMap();
        Map<String, Integer> requirementMap = loadApprovedRequirementMap();

        JTextField codeField = new JTextField(currentCode);
        JTextField nameField = new JTextField(currentName);
        JComboBox<String> wbsCombo = new JComboBox<>(wbsMap.keySet().toArray(new String[0]));
        JComboBox<String> assigneeCombo = new JComboBox<>(memberMap.keySet().toArray(new String[0]));
        JComboBox<String> requirementCombo = new JComboBox<>(requirementMap.keySet().toArray(new String[0]));
        requirementCombo.insertItemAt("(None)", 0);
        requirementCombo.setSelectedItem("(None)");
        JTextField durationField = new JTextField(String.valueOf(currentDuration));
        JTextField startDateField = new JTextField(currentStartDate);
        JTextField endDateField = new JTextField(currentEndDate);
        JTextField baselineStartField = new JTextField(currentBaselineStart);
        JTextField baselineEndField = new JTextField(currentBaselineEnd);
        JTextField percentField = new JTextField(String.valueOf(currentPercent));
        JTextField plannedCostField = new JTextField(currentPlannedCost.toPlainString());
        JTextField actualCostField = new JTextField(currentActualCost.toPlainString());
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Not Started", "In Progress", "Completed"});
        statusCombo.setSelectedItem(currentStatus);

        if (currentWbsId > 0) {
            setComboSelection(wbsCombo, wbsMap, currentWbsId);
        }
        if (currentAssigneeId > 0) {
            setComboSelection(assigneeCombo, memberMap, currentAssigneeId);
        }
        if (currentRequirementId > 0) {
            setComboSelection(requirementCombo, requirementMap, currentRequirementId);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("WBS item:"));
        panel.add(wbsCombo);
        panel.add(new JLabel("Assignee:"));
        panel.add(assigneeCombo);
        panel.add(new JLabel("Linked approved requirement:"));
        panel.add(requirementCombo);
        panel.add(new JLabel("Duration days:"));
        panel.add(durationField);
        panel.add(new JLabel("Start date (yyyy-MM-dd):"));
        panel.add(startDateField);
        panel.add(new JLabel("End date (yyyy-MM-dd):"));
        panel.add(endDateField);
        panel.add(new JLabel("Baseline start (yyyy-MM-dd):"));
        panel.add(baselineStartField);
        panel.add(new JLabel("Baseline end (yyyy-MM-dd):"));
        panel.add(baselineEndField);
        panel.add(new JLabel("Percent complete:"));
        panel.add(percentField);
        panel.add(new JLabel("Planned cost:"));
        panel.add(plannedCostField);
        panel.add(new JLabel("Actual cost:"));
        panel.add(actualCostField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit activity" : "Add activity", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            Integer wbsId = wbsMap.get((String) wbsCombo.getSelectedItem());
            Integer assigneeId = memberMap.get((String) assigneeCombo.getSelectedItem());
            Integer requirementId = requirementMap.get((String) requirementCombo.getSelectedItem());
            if ("(None)".equals(requirementCombo.getSelectedItem())) {
                requirementId = null;
            }
            int duration = parsePositiveInt(durationField.getText().trim(), "Duration days");
            LocalDate start = parseOptionalDate(startDateField.getText().trim(), "Start date");
            LocalDate end = parseOptionalDate(endDateField.getText().trim(), "End date");
            LocalDate baselineStart = parseOptionalDate(baselineStartField.getText().trim(), "Baseline start");
            LocalDate baselineEnd = parseOptionalDate(baselineEndField.getText().trim(), "Baseline end");
            int percent = parsePercent(percentField.getText().trim(), "Percent complete");
            BigDecimal plannedCost = parseBigDecimal(plannedCostField.getText().trim(), "Planned cost");
            BigDecimal actualCost = parseBigDecimal(actualCostField.getText().trim(), "Actual cost");
            String status = (String) statusCombo.getSelectedItem();

            if (code.isEmpty() || name.isEmpty() || wbsId == null || status == null) {
                showError("Code, name, WBS item, and status are required.");
                return;
            }
            if (start != null && end != null && end.isBefore(start)) {
                showError("End date must be after the start date.");
                return;
            }
            if (baselineStart != null && baselineEnd != null && baselineEnd.isBefore(baselineStart)) {
                showError("Baseline end date must be after the baseline start date.");
                return;
            }
            if (duration <= 0) {
                showError("Duration days must be greater than zero.");
                return;
            }
            if (percent < 0 || percent > 100) {
                showError("Percent complete must be between 0 and 100.");
                return;
            }

            try (Connection c = Database.get()) {
                if (edit) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE activity SET code = ?, name = ?, wbs_id = ?, assignee_id = ?, requirement_id = ?, duration_days = ?, start_date = ?, end_date = ?, baseline_start = ?, baseline_end = ?, percent_complete = ?, planned_cost = ?, actual_cost = ?, status = ? WHERE id = ? AND project_id = ?")) {
                        ps.setString(1, code);
                        ps.setString(2, name);
                        ps.setInt(3, wbsId);
                        if (assigneeId != null) ps.setInt(4, assigneeId); else ps.setNull(4, java.sql.Types.INTEGER);
                        if (requirementId != null) ps.setInt(5, requirementId); else ps.setNull(5, java.sql.Types.INTEGER);
                        ps.setInt(6, duration);
                        if (start != null) ps.setDate(7, java.sql.Date.valueOf(start)); else ps.setNull(7, java.sql.Types.DATE);
                        if (end != null) ps.setDate(8, java.sql.Date.valueOf(end)); else ps.setNull(8, java.sql.Types.DATE);
                        if (baselineStart != null) ps.setDate(9, java.sql.Date.valueOf(baselineStart)); else ps.setNull(9, java.sql.Types.DATE);
                        if (baselineEnd != null) ps.setDate(10, java.sql.Date.valueOf(baselineEnd)); else ps.setNull(10, java.sql.Types.DATE);
                        ps.setInt(11, percent);
                        ps.setBigDecimal(12, plannedCost);
                        ps.setBigDecimal(13, actualCost);
                        ps.setString(14, status);
                        ps.setInt(15, selectedId);
                        ps.setInt(16, PROJECT_ID);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO activity (project_id, code, name, wbs_id, assignee_id, requirement_id, duration_days, start_date, end_date, baseline_start, baseline_end, percent_complete, planned_cost, actual_cost, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, PROJECT_ID);
                        ps.setString(2, code);
                        ps.setString(3, name);
                        ps.setInt(4, wbsId);
                        if (assigneeId != null) ps.setInt(5, assigneeId); else ps.setNull(5, java.sql.Types.INTEGER);
                        if (requirementId != null) ps.setInt(6, requirementId); else ps.setNull(6, java.sql.Types.INTEGER);
                        ps.setInt(7, duration);
                        if (start != null) ps.setDate(8, java.sql.Date.valueOf(start)); else ps.setNull(8, java.sql.Types.DATE);
                        if (end != null) ps.setDate(9, java.sql.Date.valueOf(end)); else ps.setNull(9, java.sql.Types.DATE);
                        if (baselineStart != null) ps.setDate(10, java.sql.Date.valueOf(baselineStart)); else ps.setNull(10, java.sql.Types.DATE);
                        if (baselineEnd != null) ps.setDate(11, java.sql.Date.valueOf(baselineEnd)); else ps.setNull(11, java.sql.Types.DATE);
                        ps.setInt(12, percent);
                        ps.setBigDecimal(13, plannedCost);
                        ps.setBigDecimal(14, actualCost);
                        ps.setString(15, status);
                        ps.executeUpdate();
                    }
                }
                loadAll();
                statusLabel.setText(edit ? "Activity updated." : "Activity added.");
            } catch (SQLException ex) {
                showError("Unable to save activity. " + ex.getMessage());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void editSelectedActivity() {
        int row = activityTable.getSelectedRow();
        if (row < 0) {
            showError("Select an activity first.");
            return;
        }
        promptActivityDialog(true, -1);
    }

    private void deleteSelectedActivity() {
        int row = activityTable.getSelectedRow();
        if (row < 0) {
            showError("Select an activity first.");
            return;
        }
        int id = Integer.parseInt(activityModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this activity and its dependencies?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement depPs = c.prepareStatement("DELETE FROM activity_dependency WHERE activity_id = ? OR predecessor_id = ?")) {
                depPs.setInt(1, id);
                depPs.setInt(2, id);
                depPs.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM activity WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, id);
                ps.setInt(2, PROJECT_ID);
                ps.executeUpdate();
            }
            c.commit();
            loadAll();
            statusLabel.setText("Activity deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete activity. " + ex.getMessage());
        }
    }

    private void generateSchedule() {
        try (Connection c = Database.get()) {
            ScheduleEngine.ScheduleResult result = ScheduleEngine.generateProjectSchedule(c, PROJECT_ID);
            StringBuilder msg = new StringBuilder();
            msg.append("Project finish: ").append(result.projectFinishDate).append("\n");
            if (result.projectDeadline != null) {
                msg.append("Project deadline: ").append(result.projectDeadline).append("\n");
                if (result.overDeadline) {
                    msg.append("Warning: project completion is after the deadline.");
                } else {
                    msg.append("The project remains on or before the deadline.");
                }
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Schedule generated", JOptionPane.INFORMATION_MESSAGE);
            loadAll();
        } catch (SQLException ex) {
            showError("Unable to generate schedule. " + ex.getMessage());
        }
    }

    private void loadResourceView() {
        resourceModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT tm.name, SUM(a.duration_days) AS total_days, GROUP_CONCAT(CONCAT(a.code,'(',a.start_date,'-',a.end_date,')') SEPARATOR '; ') AS activities " +
                             "FROM team_member tm LEFT JOIN activity a ON a.assignee_id = tm.id AND a.project_id = ? " +
                             "WHERE tm.project_id = ? GROUP BY tm.id, tm.name ORDER BY tm.name")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String activities = rs.getString("activities");
                    boolean overlap = ScheduleEngine.hasResourceOverlap(c, PROJECT_ID);
                    resourceModel.addRow(new Object[]{
                            rs.getString("name"),
                            activities == null ? "" : activities,
                            rs.getInt("total_days"),
                            overlap ? "Warning" : "OK"
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load resource view. " + ex.getMessage());
        }
    }

    private void renderDelayImpact() {
        try (Connection c = Database.get()) {
            delayImpactArea.setText(ScheduleEngine.buildDelayImpactText(c, PROJECT_ID));
        } catch (SQLException ex) {
            delayImpactArea.setText("Unable to calculate delay impact. " + ex.getMessage());
        }
    }

    private void renderGantt() {
        ganttPanel.repaint();
    }

    private Map<String, Integer> loadWbsMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, name FROM wbs_item WHERE project_id = ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("code") + " - " + rs.getString("name"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load WBS list. " + ex.getMessage());
        }
        return map;
    }

    private Map<String, Integer> loadMemberMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM team_member WHERE project_id = ? ORDER BY name")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load team members. " + ex.getMessage());
        }
        return map;
    }

    private Map<String, Integer> loadApprovedRequirementMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, title FROM requirement WHERE project_id = ? AND status = 'Approved' ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("code") + " - " + rs.getString("title"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load approved requirements. " + ex.getMessage());
        }
        return map;
    }

    private void setComboSelection(JComboBox<String> combo, Map<String, Integer> map, int selectedId) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(selectedId)) {
                combo.setSelectedItem(entry.getKey());
                return;
            }
        }
    }

    private int activityIdFromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return -1;
        }
        String code = label.split(" - ")[0];
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM activity WHERE project_id = ? AND code = ?")) {
            ps.setInt(1, PROJECT_ID);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException ex) {
            showError("Unable to resolve activity id. " + ex.getMessage());
        }
        return -1;
    }

    private LocalDate parseOptionalDate(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " must use yyyy-MM-dd format.");
        }
    }

    private int parsePositiveInt(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            int result = Integer.parseInt(value.trim());
            if (result <= 0) throw new IllegalArgumentException(label + " must be greater than zero.");
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private int parsePercent(String value, String label) {
        int result = parsePositiveInt(value, label);
        if (result > 100) throw new IllegalArgumentException(label + " cannot exceed 100.");
        return result;
    }

    private BigDecimal parseBigDecimal(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            BigDecimal result = new BigDecimal(value.trim());
            if (result.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText(message);
    }

    private static class GanttPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            try {
                Connection c = Database.get();
                java.sql.Date projectStart = null;
                java.sql.Date projectDeadline = null;
                java.sql.Date statusDate = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT start_date, deadline, status_date FROM project WHERE id = ?")) {
                    ps.setInt(1, PROJECT_ID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            projectStart = rs.getDate("start_date");
                            projectDeadline = rs.getDate("deadline");
                            statusDate = rs.getDate("status_date");
                        }
                    }
                }

                List<ActivityBar> bars = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement("SELECT id, code, name, start_date, end_date, baseline_start, baseline_end, percent_complete, status FROM activity WHERE project_id = ? ORDER BY start_date")) {
                    ps.setInt(1, PROJECT_ID);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ActivityBar bar = new ActivityBar();
                            bar.id = rs.getInt("id");
                            bar.code = rs.getString("code");
                            bar.name = rs.getString("name");
                            bar.start = rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate();
                            bar.end = rs.getDate("end_date") == null ? null : rs.getDate("end_date").toLocalDate();
                            bar.baselineStart = rs.getDate("baseline_start") == null ? null : rs.getDate("baseline_start").toLocalDate();
                            bar.baselineEnd = rs.getDate("baseline_end") == null ? null : rs.getDate("baseline_end").toLocalDate();
                            bar.percent = rs.getInt("percent_complete");
                            bar.status = rs.getString("status");
                            bars.add(bar);
                        }
                    }
                }

                if (projectStart == null) {
                    return;
                }

                LocalDate startDate = projectStart.toLocalDate();
                LocalDate endDate = projectDeadline == null ? startDate.plusDays(120) : projectDeadline.toLocalDate();
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
                int leftPadding = 180;
                int topPadding = 40;
                int plotWidth = getWidth() - leftPadding - 30;
                int rowHeight = 28;
                int barHeight = 12;

                g2.setColor(Color.BLACK);
                g2.drawLine(leftPadding, topPadding, leftPadding + plotWidth, topPadding);
                for (int day = 0; day <= totalDays; day += 7) {
                    LocalDate date = startDate.plusDays(day);
                    int x = leftPadding + (int) ((double) day / totalDays * plotWidth);
                    g2.drawLine(x, topPadding, x, topPadding + bars.size() * rowHeight + 20);
                    g2.drawString(date.format(DateTimeFormatter.ISO_LOCAL_DATE), x, topPadding - 8);
                }

                if (statusDate != null) {
                    int x = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, statusDate.toLocalDate()) / totalDays * plotWidth);
                    g2.setColor(new Color(255, 140, 0));
                    g2.draw(new Line2D.Double(x, topPadding - 10, x, topPadding + 20 + bars.size() * rowHeight));
                }

                if (projectDeadline != null) {
                    int x = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, projectDeadline.toLocalDate()) / totalDays * plotWidth);
                    g2.setColor(Color.MAGENTA);
                    g2.draw(new Line2D.Double(x, topPadding - 10, x, topPadding + 20 + bars.size() * rowHeight));
                }

                int rowIndex = 0;
                for (ActivityBar bar : bars) {
                    if (bar.start == null || bar.end == null) continue;
                    int startX = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, bar.start) / totalDays * plotWidth);
                    int endX = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, bar.end) / totalDays * plotWidth);
                    int barY = topPadding + rowIndex * rowHeight + 18;

                    g2.setColor(Color.LIGHT_GRAY);
                    if (bar.baselineStart != null && bar.baselineEnd != null) {
                        int baseX = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, bar.baselineStart) / totalDays * plotWidth);
                        int baseEndX = leftPadding + (int) ((double) java.time.temporal.ChronoUnit.DAYS.between(startDate, bar.baselineEnd) / totalDays * plotWidth);
                        g2.fillRect(baseX, barY - 2, Math.max(4, baseEndX - baseX), 6);
                    }

                    if ("Completed".equals(bar.status)) g2.setColor(new Color(28, 117, 188));
                    else if ("In Progress".equals(bar.status)) g2.setColor(new Color(18, 163, 117));
                    else g2.setColor(new Color(74, 144, 226));
                    if (bar.end.isAfter(projectDeadline.toLocalDate())) g2.setColor(Color.RED);
                    g2.fillRect(startX, barY, Math.max(8, endX - startX), barHeight);

                    int progressWidth = (int) ((double) bar.percent / 100 * (endX - startX));
                    g2.setColor(new Color(255, 204, 0));
                    g2.fillRect(startX, barY, Math.max(0, progressWidth), barHeight);

                    g2.setColor(Color.BLACK);
                    g2.drawString(bar.code, startX + 3, barY - 5);
                    rowIndex++;
                }
            } catch (Exception ex) {
                g2.setColor(Color.RED);
                g2.drawString("Gantt unavailable", 20, 20);
            } finally {
                g2.dispose();
            }
        }
    }

    private static class ActivityBar {
        int id;
        String code;
        String name;
        String status;
        LocalDate start;
        LocalDate end;
        LocalDate baselineStart;
        LocalDate baselineEnd;
        int percent;
    }
}
