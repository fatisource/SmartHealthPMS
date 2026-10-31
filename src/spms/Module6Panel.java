package spms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Module6Panel extends JPanel {
    private static final int PROJECT_ID = 1;

    private final JLabel projectNameLabel = new JLabel("Project: -");
    private final JLabel projectStatusLabel = new JLabel("Status: -");
    private final JLabel budgetLabel = new JLabel("Budget: -");
    private final JLabel milestoneLabel = new JLabel("Milestones: -");
    private final JLabel requirementsLabel = new JLabel("Requirements: -");
    private final JLabel defectsLabel = new JLabel("Open defects: -");
    private final JLabel risksLabel = new JLabel("Risks: -");
    private final JLabel qualityLabel = new JLabel("Quality: -");

    private final DefaultTableModel validationModel = new DefaultTableModel(
            new Object[]{"Validation check", "Status", "Details"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable validationTable = new JTable(validationModel);

    private final JTextArea executiveSummary = new JTextArea();

    public Module6Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Project Dashboard"));
        summaryPanel.add(projectNameLabel);
        summaryPanel.add(projectStatusLabel);
        summaryPanel.add(budgetLabel);
        summaryPanel.add(milestoneLabel);
        summaryPanel.add(requirementsLabel);
        summaryPanel.add(defectsLabel);
        summaryPanel.add(risksLabel);
        summaryPanel.add(qualityLabel);

        JPanel validationPanel = new JPanel(new BorderLayout(8, 8));
        validationPanel.setBorder(BorderFactory.createTitledBorder("Integration validation"));
        validationPanel.add(new JScrollPane(validationTable), BorderLayout.CENTER);

        JPanel summaryTextPanel = new JPanel(new BorderLayout(8, 8));
        summaryTextPanel.setBorder(BorderFactory.createTitledBorder("Executive summary"));
        executiveSummary.setEditable(false);
        executiveSummary.setLineWrap(true);
        executiveSummary.setWrapStyleWord(true);
        executiveSummary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        summaryTextPanel.add(new JScrollPane(executiveSummary), BorderLayout.CENTER);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, validationPanel, summaryTextPanel);
        centerSplit.setResizeWeight(0.6);

        JButton refreshBtn = new JButton("Refresh dashboard");
        refreshBtn.addActionListener(e -> refresh());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(refreshBtn);

        add(summaryPanel, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        try (Connection c = Database.get()) {
            DashboardData data = loadDashboardData(c);
            renderDashboard(data);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to load dashboard data. " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DashboardData loadDashboardData(Connection c) throws SQLException {
        DashboardData data = new DashboardData();

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, name, status, approved_budget, start_date, deadline, cost_threshold, schedule_threshold FROM project WHERE id = ?")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data.projectId = rs.getInt("id");
                    data.projectName = rs.getString("name");
                    data.projectStatus = rs.getString("status");
                    data.approvedBudget = rs.getBigDecimal("approved_budget");
                    data.startDate = rs.getDate("start_date");
                    data.deadline = rs.getDate("deadline");
                    data.costThreshold = rs.getBigDecimal("cost_threshold");
                    data.scheduleThreshold = rs.getBigDecimal("schedule_threshold");
                }
            }
        }

        data.totalRequirements = countInt(c, "SELECT COUNT(*) FROM requirement WHERE project_id = ?", PROJECT_ID);
        data.approvedRequirements = countInt(c, "SELECT COUNT(*) FROM requirement WHERE project_id = ? AND status = 'Approved'", PROJECT_ID);
        data.totalActivities = countInt(c, "SELECT COUNT(*) FROM activity WHERE project_id = ?", PROJECT_ID);
        data.completedActivities = countInt(c, "SELECT COUNT(*) FROM activity WHERE project_id = ? AND status = 'Completed'", PROJECT_ID);
        data.totalDefects = countInt(c, "SELECT COUNT(*) FROM defect WHERE project_id = ?", PROJECT_ID);
        data.openDefects = countInt(c, "SELECT COUNT(*) FROM defect WHERE project_id = ? AND status NOT IN ('Resolved','Closed')", PROJECT_ID);
        data.totalRisks = countInt(c, "SELECT COUNT(*) FROM risk WHERE project_id = ?", PROJECT_ID);
        data.openRisks = countInt(c, "SELECT COUNT(*) FROM risk WHERE project_id = ? AND status IN ('Open','Mitigated','Occurred')", PROJECT_ID);
        data.totalQualityChecks = countInt(c, "SELECT COUNT(*) FROM quality_check WHERE project_id = ?", PROJECT_ID);
        data.passedQualityChecks = countInt(c, "SELECT COUNT(*) FROM quality_check WHERE project_id = ? AND result = 'Pass'", PROJECT_ID);
        data.totalMilestones = countInt(c, "SELECT COUNT(*) FROM milestone WHERE project_id = ?", PROJECT_ID);
        data.achievedMilestones = countInt(c, "SELECT COUNT(*) FROM milestone WHERE project_id = ? AND status = 'Achieved'", PROJECT_ID);
        data.pendingChanges = countInt(c, "SELECT COUNT(*) FROM change_request WHERE project_id = ? AND status = 'Pending'", PROJECT_ID);

        data.validationRows = new ArrayList<>();
        data.validationRows.add(new ValidationRow("Project baseline defined", data.projectName != null ? "Pass" : "Fail",
                data.projectName != null ? "Project record exists and is active." : "No project record found for this dashboard."));
        data.validationRows.add(new ValidationRow("Requirements baseline", data.approvedRequirements > 0 ? "Pass" : "Warning",
                data.approvedRequirements + " approved requirement(s) in the project."));
        data.validationRows.add(new ValidationRow("Schedule readiness", data.totalActivities > 0 ? "Pass" : "Warning",
                data.totalActivities + " activity record(s) are tracked against the WBS and schedule."));
        data.validationRows.add(new ValidationRow("Defect control", data.openDefects <= 5 ? "Pass" : "Warning",
                data.openDefects + " open defect(s) require follow-up."));
        data.validationRows.add(new ValidationRow("Quality assurance", data.totalQualityChecks > 0 ? (data.passedQualityChecks * 100.0 / data.totalQualityChecks >= 80 ? "Pass" : "Warning") : "Warning",
                data.totalQualityChecks + " quality check(s); " + data.passedQualityChecks + " passed."));
        data.validationRows.add(new ValidationRow("Risk response", data.openRisks <= 3 ? "Pass" : "Warning",
                data.openRisks + " open or active risk(s) under ownership and treatment."));
        data.validationRows.add(new ValidationRow("Change control", data.pendingChanges <= 2 ? "Pass" : "Warning",
                data.pendingChanges + " adjustment request(s) are waiting for approval."));

        return data;
    }

    private void renderDashboard(DashboardData data) {
        projectNameLabel.setText("Project: " + (data.projectName == null ? "-" : data.projectName));
        projectStatusLabel.setText("Status: " + (data.projectStatus == null ? "-" : data.projectStatus));
        budgetLabel.setText("Budget: " + money(data.approvedBudget));
        milestoneLabel.setText("Milestones: " + data.achievedMilestones + "/" + data.totalMilestones + " achieved");
        requirementsLabel.setText("Requirements: " + data.approvedRequirements + "/" + data.totalRequirements + " approved");
        defectsLabel.setText("Open defects: " + data.openDefects + " / " + data.totalDefects);
        risksLabel.setText("Risks: " + data.openRisks + " active");
        qualityLabel.setText("Quality: " + (data.totalQualityChecks == 0 ? "0%" : percent(data.passedQualityChecks, data.totalQualityChecks)) + " pass");

        validationModel.setRowCount(0);
        for (ValidationRow row : data.validationRows) {
            validationModel.addRow(new Object[]{row.check, row.status, row.details});
        }

        executiveSummary.setText(buildExecutiveSummary(data));
    }

    private String buildExecutiveSummary(DashboardData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Project health summary for ").append(data.projectName == null ? "the active project" : data.projectName).append("\n\n");
        sb.append("Current status: ").append(data.projectStatus == null ? "Unknown" : data.projectStatus).append("\n");
        sb.append("Approved budget: ").append(money(data.approvedBudget)).append("\n");
        sb.append("Completed activities: ").append(data.completedActivities).append(" of ").append(data.totalActivities).append("\n");
        sb.append("Approved requirements: ").append(data.approvedRequirements).append(" of ").append(data.totalRequirements).append("\n");
        sb.append("Open defects: ").append(data.openDefects).append(" | Active risks: ").append(data.openRisks).append("\n");
        sb.append("Quality compliance: ").append(data.totalQualityChecks == 0 ? "No checks recorded" : percent(data.passedQualityChecks, data.totalQualityChecks) + " pass rate").append("\n");
        sb.append("Pending change requests: ").append(data.pendingChanges).append("\n\n");
        sb.append("Recommendation: ");
        if (data.openDefects <= 2 && data.openRisks <= 3 && data.pendingChanges <= 2) {
            sb.append("The project is stable and ready for continued execution or final review.");
        } else if (data.openDefects <= 5 && data.openRisks <= 5) {
            sb.append("The project remains manageable, but defect closure and risk mitigation should be tracked weekly.");
        } else {
            sb.append("Immediate attention is required to reduce unresolved defects, active risks, and pending change approvals.");
        }
        return sb.toString();
    }

    private int countInt(Connection c, String sql, int projectId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "PKR 0.00";
        }
        return "PKR " + new DecimalFormat("#,##0.00").format(value.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private String percent(int value, int total) {
        if (total <= 0) {
            return "0%";
        }
        return String.format("%.0f%%", (value * 100.0) / total);
    }

    private static class DashboardData {
        int projectId;
        String projectName;
        String projectStatus;
        BigDecimal approvedBudget;
        java.sql.Date startDate;
        java.sql.Date deadline;
        BigDecimal costThreshold;
        BigDecimal scheduleThreshold;
        int totalRequirements;
        int approvedRequirements;
        int totalActivities;
        int completedActivities;
        int totalDefects;
        int openDefects;
        int totalRisks;
        int openRisks;
        int totalQualityChecks;
        int passedQualityChecks;
        int totalMilestones;
        int achievedMilestones;
        int pendingChanges;
        List<ValidationRow> validationRows;
    }

    private static class ValidationRow {
        String check;
        String status;
        String details;

        ValidationRow(String check, String status, String details) {
            this.check = check;
            this.status = status;
            this.details = details;
        }
    }
}
