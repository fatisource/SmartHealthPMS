package spms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final Color NAVY = new Color(22, 38, 63);
    private static final Color MUTED = new Color(113, 128, 150);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Font DASHBOARD_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final MetricCard projectCard = new MetricCard("PROJECT STATUS", "-", "Current project lifecycle");
    private final MetricCard activityCard = new MetricCard("ACTIVITY PROGRESS", "-", "Activities completed");
    private final MetricCard requirementCard = new MetricCard("APPROVED REQUIREMENTS", "-", "Approved baseline");
    private final MetricCard milestoneCard = new MetricCard("MILESTONES", "-", "Milestones achieved");
    private final MetricCard defectCard = new MetricCard("OPEN DEFECTS", "-", "Items needing follow-up");
    private final MetricCard riskCard = new MetricCard("ACTIVE RISKS", "-", "Risks being monitored");
    private final MetricCard qualityCard = new MetricCard("QUALITY CHECKS", "-", "Inspection pass rate");
    private final MetricCard budgetCard = new MetricCard("APPROVED BUDGET", "-", "Project budget baseline");

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
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JPanel validationPanel = new JPanel(new BorderLayout(8, 8));
        validationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        validationPanel.setBackground(Color.WHITE);
        JLabel validationHeading = sectionHeading("Readiness checks");
        validationPanel.add(validationHeading, BorderLayout.NORTH);
        validationTable.setRowHeight(34);
        validationTable.setShowVerticalLines(false);
        validationTable.setGridColor(new Color(235, 239, 244));
        validationTable.setFillsViewportHeight(true);
        validationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        validationTable.getTableHeader().setReorderingAllowed(false);
        validationTable.getTableHeader().setPreferredSize(new Dimension(0, 34));
        validationTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                if (!selected) {
                    if (column == 1) {
                        String status = String.valueOf(value);
                        cell.setForeground("Pass".equals(status) ? new Color(20, 112, 95)
                                : "Fail".equals(status) ? new Color(170, 55, 45) : new Color(150, 100, 18));
                        setFont(DASHBOARD_FONT.deriveFont(Font.BOLD));
                    } else {
                        cell.setForeground(column == 0 ? NAVY : MUTED);
                        setFont(DASHBOARD_FONT);
                    }
                    cell.setBackground(Color.WHITE);
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return cell;
            }
        });
        validationPanel.add(new JScrollPane(validationTable), BorderLayout.CENTER);

        JPanel summaryTextPanel = new JPanel(new BorderLayout(8, 8));
        summaryTextPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        summaryTextPanel.setBackground(Color.WHITE);
        summaryTextPanel.add(sectionHeading("Manager's brief"), BorderLayout.NORTH);
        executiveSummary.setEditable(false);
        executiveSummary.setLineWrap(true);
        executiveSummary.setWrapStyleWord(true);
        executiveSummary.setFont(DASHBOARD_FONT.deriveFont(14f));
        executiveSummary.setForeground(NAVY);
        executiveSummary.setBackground(new Color(248, 250, 252));
        executiveSummary.setBorder(new EmptyBorder(14, 14, 14, 14));
        summaryTextPanel.add(new JScrollPane(executiveSummary), BorderLayout.CENTER);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, validationPanel, summaryTextPanel);
        centerSplit.setResizeWeight(0.62);
        centerSplit.setBorder(null);
        centerSplit.setContinuousLayout(true);

        JButton refreshBtn = new JButton("Refresh dashboard");
        refreshBtn.setFont(DASHBOARD_FONT.deriveFont(Font.BOLD, 12f));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(new Color(20, 145, 137));
        refreshBtn.setBorder(new EmptyBorder(9, 15, 9, 15));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBackground(Color.WHITE);
        JPanel topHeading = new JPanel();
        topHeading.setOpaque(false);
        topHeading.setLayout(new BoxLayout(topHeading, BoxLayout.Y_AXIS));
        JLabel overview = sectionHeading("Project at a glance");
        JLabel explanation = new JLabel("Quickly review delivery progress and items that may need attention.");
        explanation.setFont(DASHBOARD_FONT);
        explanation.setForeground(MUTED);
        explanation.setBorder(new EmptyBorder(4, 0, 0, 0));
        topHeading.add(overview);
        topHeading.add(explanation);
        top.add(topHeading, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(2, 4, 10, 10));
        metrics.setOpaque(false);
        metrics.add(projectCard);
        metrics.add(activityCard);
        metrics.add(requirementCard);
        metrics.add(milestoneCard);
        metrics.add(defectCard);
        metrics.add(riskCard);
        metrics.add(qualityCard);
        metrics.add(budgetCard);

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setBackground(Color.WHITE);
        north.add(top, BorderLayout.NORTH);
        north.add(metrics, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);

        refresh();
    }

    private JLabel sectionHeading(String text) {
        JLabel heading = new JLabel(text);
        heading.setFont(DASHBOARD_FONT.deriveFont(Font.BOLD, 16f));
        heading.setForeground(NAVY);
        return heading;
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
        data.criticalOpenDefects = countInt(c, "SELECT COUNT(*) FROM defect WHERE project_id = ? AND severity = 'Critical' AND status NOT IN ('Resolved','Closed')", PROJECT_ID);
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
        data.validationRows.add(new ValidationRow("Critical defect follow-up", data.criticalOpenDefects == 0 ? "Pass" : "Warning",
                data.criticalOpenDefects + " unresolved critical defect(s) need priority review."));
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
        projectCard.setMetric(data.projectStatus == null ? "Not found" : data.projectStatus,
                data.projectName == null ? "No project record for project ID " + PROJECT_ID : data.projectName);
        activityCard.setMetric(data.completedActivities + " / " + data.totalActivities,
                data.totalActivities == 0 ? "No activities recorded" : percent(data.completedActivities, data.totalActivities) + " complete");
        requirementCard.setMetric(data.approvedRequirements + " / " + data.totalRequirements,
                data.totalRequirements == 0 ? "No requirements recorded" : "Approved and ready for delivery");
        milestoneCard.setMetric(data.achievedMilestones + " / " + data.totalMilestones,
                data.totalMilestones == 0 ? "No milestones recorded" : "Milestones achieved");
        defectCard.setMetric(Integer.toString(data.openDefects),
                data.criticalOpenDefects + " critical, " + data.openDefects + " open total");
        defectCard.setAccent(data.criticalOpenDefects > 0 ? new Color(183, 62, 54)
                : data.openDefects > 0 ? new Color(194, 128, 35) : new Color(20, 145, 137));
        riskCard.setMetric(Integer.toString(data.openRisks),
                data.openRisks + " active of " + data.totalRisks + " total");
        riskCard.setAccent(data.openRisks > 0 ? new Color(194, 128, 35) : new Color(20, 145, 137));
        qualityCard.setMetric(data.totalQualityChecks == 0 ? "No data" : percent(data.passedQualityChecks, data.totalQualityChecks),
                data.passedQualityChecks + " passed of " + data.totalQualityChecks + " checks");
        budgetCard.setMetric(money(data.approvedBudget), "Approved project budget");

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
        sb.append("Suggested next steps\n");
        int actions = 0;
        if (data.openDefects > 0) {
            sb.append(data.criticalOpenDefects > 0
                    ? "- Prioritize the " + data.criticalOpenDefects + " unresolved critical defect(s), then review remaining open defects.\n"
                    : "- Review open defects and confirm owners and follow-up dates.\n");
            actions++;
        }
        if (data.openRisks > 0) {
            sb.append("- Review active risks and confirm response actions with their owners.\n");
            actions++;
        }
        if (data.pendingChanges > 0) {
            sb.append("- Review pending change requests through the change-control process.\n");
            actions++;
        }
        if (data.totalQualityChecks == 0 || data.passedQualityChecks < data.totalQualityChecks) {
            sb.append("- Review failed or pending quality checks before sign-off.\n");
            actions++;
        }
        if (actions == 0) {
            sb.append("- No open follow-up items are recorded. Continue routine project monitoring.\n");
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
        int criticalOpenDefects;
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

    private static class MetricCard extends JPanel {
        private final JLabel valueLabel = new JLabel();
        private final JLabel detailLabel = new JLabel();
        private final JPanel stripe = new JPanel();

        MetricCard(String title, String value, String detail) {
            setLayout(new BorderLayout(0, 7));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(11, 12, 11, 12)));
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(DASHBOARD_FONT.deriveFont(Font.BOLD, 10f));
            titleLabel.setForeground(MUTED);
            valueLabel.setText(value);
            valueLabel.setFont(DASHBOARD_FONT.deriveFont(Font.BOLD, 20f));
            valueLabel.setForeground(NAVY);
            detailLabel.setText(detail);
            detailLabel.setFont(DASHBOARD_FONT.deriveFont(11f));
            detailLabel.setForeground(MUTED);
            JPanel text = new JPanel(new BorderLayout(0, 5));
            text.setOpaque(false);
            text.add(titleLabel, BorderLayout.NORTH);
            text.add(valueLabel, BorderLayout.CENTER);
            text.add(detailLabel, BorderLayout.SOUTH);
            stripe.setBackground(new Color(20, 145, 137));
            stripe.setPreferredSize(new Dimension(4, 0));
            add(stripe, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            setMetric(value, detail);
        }

        void setMetric(String value, String detail) {
            valueLabel.setText(value);
            detailLabel.setText(detail);
        }

        void setAccent(Color color) {
            stripe.setBackground(color);
        }
    }
}
