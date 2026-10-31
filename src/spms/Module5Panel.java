package spms;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module5Panel extends JPanel {
    private static final int PROJECT_ID = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DefaultTableModel defectModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Title", "Severity", "Status", "Assignee", "Activity", "Requirement", "Reported", "Resolved"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable defectTable = new JTable(defectModel);

    private final DefaultTableModel qualityModel = new DefaultTableModel(
            new Object[]{"ID", "WBS", "Checklist Item", "Result", "Inspector", "Inspection Date", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable qualityTable = new JTable(qualityModel);

    private final DefaultTableModel riskModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Description", "Likelihood", "Impact", "Score", "Level", "Owner", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable riskTable = new JTable(riskModel);

    private final JPanel matrixPanel = new JPanel(new GridLayout(6, 6, 3, 3));
    private final JTree riskTree = new JTree(new DefaultMutableTreeNode("Risks"));
    private final JLabel passRateLabel = new JLabel("Pass rate: -");

    public Module5Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel defectsPanel = buildDefectsPanel();
        JPanel qualityPanel = buildQualityPanel();
        JPanel riskPanel = buildRiskPanel();

        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, defectsPanel, qualityPanel);
        topSplit.setResizeWeight(0.55);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topSplit, riskPanel);
        mainSplit.setResizeWeight(0.7);
        add(mainSplit, BorderLayout.CENTER);

        loadAll();
    }

    private JPanel buildDefectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Defects"));

        defectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String severity = String.valueOf(defectModel.getValueAt(row, 3));
                String status = String.valueOf(defectModel.getValueAt(row, 4));
                boolean unresolvedCritical = "Critical".equals(severity) && !("Resolved".equals(status) || "Closed".equals(status));
                if (unresolvedCritical) {
                    c.setBackground(new Color(255, 210, 210));
                    c.setForeground(Color.BLACK);
                } else if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptDefectDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedDefect());
        deleteBtn.addActionListener(e -> deleteSelectedDefect());
        refreshBtn.addActionListener(e -> loadDefects());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);

        panel.add(new JScrollPane(defectTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildQualityPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Quality Checklist"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(passRateLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptQualityDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedQuality());
        deleteBtn.addActionListener(e -> deleteSelectedQuality());
        refreshBtn.addActionListener(e -> loadQualityChecks());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(qualityTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRiskPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Risk Register"));

        JPanel matrixWrapper = new JPanel(new BorderLayout(8, 8));
        matrixWrapper.setBorder(BorderFactory.createTitledBorder("Prioritization Matrix"));
        matrixWrapper.add(matrixPanel, BorderLayout.CENTER);

        JPanel riskTablePanel = new JPanel(new BorderLayout(8, 8));
        riskTablePanel.setBorder(BorderFactory.createTitledBorder("Risks"));
        riskTablePanel.add(new JScrollPane(riskTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptRiskDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedRisk());
        deleteBtn.addActionListener(e -> deleteSelectedRisk());
        refreshBtn.addActionListener(e -> loadRiskRegister());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);

        JPanel top = new JPanel(new GridLayout(2, 1, 8, 8));
        top.add(matrixWrapper);
        top.add(riskTablePanel);

        JPanel treeWrap = new JPanel(new BorderLayout(8, 8));
        treeWrap.setBorder(BorderFactory.createTitledBorder("PDPC View"));
        treeWrap.add(new JScrollPane(riskTree), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, treeWrap);
        split.setResizeWeight(0.65);
        panel.add(split, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAll() {
        loadDefects();
        loadQualityChecks();
        loadRiskRegister();
    }

    private void loadDefects() {
        defectModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT d.id, d.code, d.title, d.severity, d.status, tm.name AS assignee_name, a.code AS activity_code, r.code AS req_code, d.reported_date, d.resolved_date " +
                             "FROM defect d " +
                             "LEFT JOIN team_member tm ON tm.id = d.assigned_to " +
                             "LEFT JOIN activity a ON a.id = d.activity_id " +
                             "LEFT JOIN requirement r ON r.id = d.requirement_id " +
                             "WHERE d.project_id = ? ORDER BY d.id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    defectModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("severity"),
                            rs.getString("status"),
                            rs.getString("assignee_name"),
                            rs.getString("activity_code"),
                            rs.getString("req_code"),
                            rs.getDate("reported_date") == null ? "" : rs.getDate("reported_date").toLocalDate().format(DATE_FORMAT),
                            rs.getDate("resolved_date") == null ? "" : rs.getDate("resolved_date").toLocalDate().format(DATE_FORMAT)
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load defect list. " + ex.getMessage());
        }
    }

    private void promptDefectDialog(boolean edit, int selectedId) {
        String currentCode = "";
        String currentTitle = "";
        String currentSeverity = "Medium";
        String currentStatus = "Open";
        int currentAssigneeId = -1;
        int currentActivityId = -1;
        int currentRequirementId = -1;
        String currentReportedDate = LocalDate.now().format(DATE_FORMAT);
        String currentResolvedDate = "";

        if (edit) {
            int row = defectTable.getSelectedRow();
            if (row < 0) {
                showError("Select a defect first.");
                return;
            }
            selectedId = Integer.parseInt(defectModel.getValueAt(row, 0).toString());
            currentCode = defectModel.getValueAt(row, 1).toString();
            currentTitle = defectModel.getValueAt(row, 2).toString();
            currentSeverity = defectModel.getValueAt(row, 3).toString();
            currentStatus = defectModel.getValueAt(row, 4).toString();
            currentReportedDate = defectModel.getValueAt(row, 8).toString();
            currentResolvedDate = defectModel.getValueAt(row, 9) == null ? "" : defectModel.getValueAt(row, 9).toString();
        }

        Map<String, Integer> memberMap = loadMemberMap();
        Map<String, Integer> activityMap = loadActivityMap();
        Map<String, Integer> requirementMap = loadApprovedRequirementMap();

        JTextField codeField = new JTextField(currentCode);
        JTextField titleField = new JTextField(currentTitle);
        JComboBox<String> severityCombo = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        severityCombo.setSelectedItem(currentSeverity);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Open", "In Progress", "Resolved", "Closed"});
        statusCombo.setSelectedItem(currentStatus);
        JComboBox<String> assigneeCombo = new JComboBox<>(memberMap.keySet().toArray(new String[0]));
        JComboBox<String> activityCombo = new JComboBox<>(activityMap.keySet().toArray(new String[0]));
        activityCombo.insertItemAt("(None)", 0);
        activityCombo.setSelectedItem("(None)");
        JComboBox<String> requirementCombo = new JComboBox<>(requirementMap.keySet().toArray(new String[0]));
        requirementCombo.insertItemAt("(None)", 0);
        requirementCombo.setSelectedItem("(None)");
        JTextField reportedDateField = new JTextField(currentReportedDate);
        JTextField resolvedDateField = new JTextField(currentResolvedDate);

        if (edit) {
            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement("SELECT assigned_to, activity_id, requirement_id, resolved_date FROM defect WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, selectedId);
                ps.setInt(2, PROJECT_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentAssigneeId = rs.getInt("assigned_to");
                        currentActivityId = rs.getInt("activity_id");
                        currentRequirementId = rs.getInt("requirement_id");
                        java.sql.Date resolved = rs.getDate("resolved_date");
                        if (resolved != null) {
                            currentResolvedDate = resolved.toLocalDate().format(DATE_FORMAT);
                            resolvedDateField.setText(currentResolvedDate);
                        }
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load defect details. " + ex.getMessage());
                return;
            }
            setComboSelection(assigneeCombo, memberMap, currentAssigneeId);
            setComboSelection(activityCombo, activityMap, currentActivityId);
            setComboSelection(requirementCombo, requirementMap, currentRequirementId);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Severity:"));
        panel.add(severityCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Assignee:"));
        panel.add(assigneeCombo);
        panel.add(new JLabel("Linked activity:"));
        panel.add(activityCombo);
        panel.add(new JLabel("Linked requirement:"));
        panel.add(requirementCombo);
        panel.add(new JLabel("Reported date (yyyy-MM-dd):"));
        panel.add(reportedDateField);
        panel.add(new JLabel("Resolved date (yyyy-MM-dd):"));
        panel.add(resolvedDateField);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit defect" : "Add defect", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String severity = (String) severityCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            Integer assigneeId = memberMap.get((String) assigneeCombo.getSelectedItem());
            Integer activityId = activityMap.get((String) activityCombo.getSelectedItem());
            Integer requirementId = requirementMap.get((String) requirementCombo.getSelectedItem());
            if ("(None)".equals(activityCombo.getSelectedItem())) {
                activityId = null;
            }
            if ("(None)".equals(requirementCombo.getSelectedItem())) {
                requirementId = null;
            }
            LocalDate reportedDate = parseDate(reportedDateField.getText().trim(), "Reported date");
            LocalDate resolvedDate = resolvedDateField.getText().trim().isEmpty() ? null : parseDate(resolvedDateField.getText().trim(), "Resolved date");

            if (code.isEmpty() || title.isEmpty() || severity == null || status == null) {
                showError("Code, title, severity and status are required.");
                return;
            }
            if (("Resolved".equals(status) || "Closed".equals(status)) && resolvedDate == null) {
                showError("Resolved or Closed defects must have a resolved date.");
                return;
            }
            if (!"Resolved".equals(status) && !"Closed".equals(status) && resolvedDate != null) {
                showError("Only Resolved or Closed defects can have a resolved date.");
                return;
            }

            try (Connection c = Database.get()) {
                if (edit) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE defect SET code = ?, title = ?, severity = ?, status = ?, assigned_to = ?, activity_id = ?, requirement_id = ?, reported_date = ?, resolved_date = ? WHERE id = ? AND project_id = ?")) {
                        ps.setString(1, code);
                        ps.setString(2, title);
                        ps.setString(3, severity);
                        ps.setString(4, status);
                        if (assigneeId != null) ps.setInt(5, assigneeId); else ps.setNull(5, java.sql.Types.INTEGER);
                        if (activityId != null) ps.setInt(6, activityId); else ps.setNull(6, java.sql.Types.INTEGER);
                        if (requirementId != null) ps.setInt(7, requirementId); else ps.setNull(7, java.sql.Types.INTEGER);
                        ps.setDate(8, java.sql.Date.valueOf(reportedDate));
                        if (resolvedDate != null) ps.setDate(9, java.sql.Date.valueOf(resolvedDate)); else ps.setNull(9, java.sql.Types.DATE);
                        ps.setInt(10, selectedId);
                        ps.setInt(11, PROJECT_ID);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO defect (project_id, code, title, severity, status, assigned_to, activity_id, requirement_id, reported_date, resolved_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, PROJECT_ID);
                        ps.setString(2, code);
                        ps.setString(3, title);
                        ps.setString(4, severity);
                        ps.setString(5, status);
                        if (assigneeId != null) ps.setInt(6, assigneeId); else ps.setNull(6, java.sql.Types.INTEGER);
                        if (activityId != null) ps.setInt(7, activityId); else ps.setNull(7, java.sql.Types.INTEGER);
                        if (requirementId != null) ps.setInt(8, requirementId); else ps.setNull(8, java.sql.Types.INTEGER);
                        ps.setDate(9, java.sql.Date.valueOf(reportedDate));
                        if (resolvedDate != null) ps.setDate(10, java.sql.Date.valueOf(resolvedDate)); else ps.setNull(10, java.sql.Types.DATE);
                        ps.executeUpdate();
                    }
                }
                loadDefects();
            } catch (SQLException ex) {
                showError("Unable to save defect. " + ex.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void editSelectedDefect() {
        int row = defectTable.getSelectedRow();
        if (row < 0) {
            showError("Select a defect first.");
            return;
        }
        promptDefectDialog(true, -1);
    }

    private void deleteSelectedDefect() {
        int row = defectTable.getSelectedRow();
        if (row < 0) {
            showError("Select a defect first.");
            return;
        }
        int id = Integer.parseInt(defectModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this defect?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM defect WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadDefects();
        } catch (SQLException ex) {
            showError("Unable to delete defect. " + ex.getMessage());
        }
    }

    private void loadQualityChecks() {
        qualityModel.setRowCount(0);
        int passCount = 0;
        int totalCount = 0;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT qc.id, w.code AS wbs_code, w.name AS wbs_name, qc.item, qc.result, tm.name AS inspector_name, qc.inspection_date, qc.notes " +
                             "FROM quality_check qc " +
                             "LEFT JOIN wbs_item w ON w.id = qc.wbs_id " +
                             "LEFT JOIN team_member tm ON tm.id = qc.inspector_id " +
                             "WHERE qc.project_id = ? ORDER BY qc.id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totalCount++;
                    String result = rs.getString("result");
                    if ("Pass".equals(result)) {
                        passCount++;
                    }
                    qualityModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("wbs_code") == null ? "" : rs.getString("wbs_code") + " - " + (rs.getString("wbs_name") == null ? "" : rs.getString("wbs_name")),
                            rs.getString("item"),
                            result,
                            rs.getString("inspector_name"),
                            rs.getDate("inspection_date") == null ? "" : rs.getDate("inspection_date").toLocalDate().format(DATE_FORMAT),
                            rs.getString("notes")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load quality checklist. " + ex.getMessage());
        }

        if (totalCount == 0) {
            passRateLabel.setText("Pass rate: 0.00%");
        } else {
            double passRate = (passCount * 100.0) / totalCount;
            passRateLabel.setText("Pass rate: " + String.format("%.2f%%", passRate));
        }
    }

    private void promptQualityDialog(boolean edit, int selectedId) {
        String currentItem = "";
        String currentResult = "Pending";
        String currentNotes = "";
        String currentInspectionDate = "";
        int currentWbsId = -1;
        int currentInspectorId = -1;

        if (edit) {
            int row = qualityTable.getSelectedRow();
            if (row < 0) {
                showError("Select a checklist item first.");
                return;
            }
            selectedId = Integer.parseInt(qualityModel.getValueAt(row, 0).toString());
            currentItem = qualityModel.getValueAt(row, 2).toString();
            currentResult = qualityModel.getValueAt(row, 3).toString();
            currentNotes = qualityModel.getValueAt(row, 6) == null ? "" : qualityModel.getValueAt(row, 6).toString();
            currentInspectionDate = qualityModel.getValueAt(row, 5) == null ? "" : qualityModel.getValueAt(row, 5).toString();
        }

        Map<String, Integer> wbsMap = loadWbsMap();
        Map<String, Integer> memberMap = loadMemberMap();

        JComboBox<String> wbsCombo = new JComboBox<>(wbsMap.keySet().toArray(new String[0]));
        JTextArea itemArea = new JTextArea(currentItem, 4, 35);
        JComboBox<String> resultCombo = new JComboBox<>(new String[]{"Pending", "Pass", "Fail"});
        resultCombo.setSelectedItem(currentResult);
        JComboBox<String> inspectorCombo = new JComboBox<>(memberMap.keySet().toArray(new String[0]));
        inspectorCombo.insertItemAt("(None)", 0);
        inspectorCombo.setSelectedItem("(None)");
        JTextField inspectionDateField = new JTextField(currentInspectionDate);
        JTextArea notesArea = new JTextArea(currentNotes, 3, 35);

        if (edit) {
            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement("SELECT wbs_id, inspector_id, inspection_date, notes FROM quality_check WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, selectedId);
                ps.setInt(2, PROJECT_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentWbsId = rs.getInt("wbs_id");
                        currentInspectorId = rs.getInt("inspector_id");
                        java.sql.Date dt = rs.getDate("inspection_date");
                        if (dt != null) {
                            currentInspectionDate = dt.toLocalDate().format(DATE_FORMAT);
                            inspectionDateField.setText(currentInspectionDate);
                        }
                        currentNotes = rs.getString("notes") == null ? "" : rs.getString("notes");
                        notesArea.setText(currentNotes);
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load quality item. " + ex.getMessage());
                return;
            }
            setComboSelection(wbsCombo, wbsMap, currentWbsId);
            setComboSelection(inspectorCombo, memberMap, currentInspectorId);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("WBS item:"));
        panel.add(wbsCombo);
        panel.add(new JLabel("Checklist item:"));
        panel.add(new JScrollPane(itemArea));
        panel.add(new JLabel("Result:"));
        panel.add(resultCombo);
        panel.add(new JLabel("Inspector:"));
        panel.add(inspectorCombo);
        panel.add(new JLabel("Inspection date (yyyy-MM-dd):"));
        panel.add(inspectionDateField);
        panel.add(new JLabel("Notes:"));
        panel.add(new JScrollPane(notesArea));

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit quality item" : "Add quality item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Integer wbsId = wbsMap.get((String) wbsCombo.getSelectedItem());
            String item = itemArea.getText().trim();
            String result = (String) resultCombo.getSelectedItem();
            Integer inspectorId = memberMap.get((String) inspectorCombo.getSelectedItem());
            if ("(None)".equals(inspectorCombo.getSelectedItem())) {
                inspectorId = null;
            }
            LocalDate inspectionDate = inspectionDateField.getText().trim().isEmpty() ? null : parseDate(inspectionDateField.getText().trim(), "Inspection date");
            String notes = notesArea.getText().trim();

            if (wbsId == null || item.isEmpty() || result == null) {
                showError("WBS item, checklist item and result are required.");
                return;
            }

            try (Connection c = Database.get()) {
                if (edit) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE quality_check SET wbs_id = ?, item = ?, result = ?, inspector_id = ?, inspection_date = ?, notes = ? WHERE id = ? AND project_id = ?")) {
                        ps.setInt(1, wbsId);
                        ps.setString(2, item);
                        ps.setString(3, result);
                        if (inspectorId != null) ps.setInt(4, inspectorId); else ps.setNull(4, java.sql.Types.INTEGER);
                        if (inspectionDate != null) ps.setDate(5, java.sql.Date.valueOf(inspectionDate)); else ps.setNull(5, java.sql.Types.DATE);
                        ps.setString(6, notes.isEmpty() ? null : notes);
                        ps.setInt(7, selectedId);
                        ps.setInt(8, PROJECT_ID);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO quality_check (project_id, wbs_id, item, result, inspector_id, inspection_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, PROJECT_ID);
                        ps.setInt(2, wbsId);
                        ps.setString(3, item);
                        ps.setString(4, result);
                        if (inspectorId != null) ps.setInt(5, inspectorId); else ps.setNull(5, java.sql.Types.INTEGER);
                        if (inspectionDate != null) ps.setDate(6, java.sql.Date.valueOf(inspectionDate)); else ps.setNull(6, java.sql.Types.DATE);
                        ps.setString(7, notes.isEmpty() ? null : notes);
                        ps.executeUpdate();
                    }
                }
                loadQualityChecks();
            } catch (SQLException ex) {
                showError("Unable to save quality checklist item. " + ex.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void editSelectedQuality() {
        int row = qualityTable.getSelectedRow();
        if (row < 0) {
            showError("Select a checklist item first.");
            return;
        }
        promptQualityDialog(true, -1);
    }

    private void deleteSelectedQuality() {
        int row = qualityTable.getSelectedRow();
        if (row < 0) {
            showError("Select a checklist item first.");
            return;
        }
        int id = Integer.parseInt(qualityModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this quality checklist item?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM quality_check WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadQualityChecks();
        } catch (SQLException ex) {
            showError("Unable to delete quality item. " + ex.getMessage());
        }
    }

    private void loadRiskRegister() {
        riskModel.setRowCount(0);
        List<RiskRow> risks = new ArrayList<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.id, r.code, r.description, r.likelihood, r.impact, r.preventive_action, r.corrective_action, tm.name AS owner_name, r.status " +
                             "FROM risk r LEFT JOIN team_member tm ON tm.id = r.owner_id WHERE r.project_id = ? ORDER BY r.id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RiskRow row = new RiskRow();
                    row.id = rs.getInt("id");
                    row.code = rs.getString("code");
                    row.description = rs.getString("description");
                    row.likelihood = rs.getInt("likelihood");
                    row.impact = rs.getInt("impact");
                    row.preventiveAction = rs.getString("preventive_action");
                    row.correctiveAction = rs.getString("corrective_action");
                    row.ownerName = rs.getString("owner_name");
                    row.status = rs.getString("status");
                    row.score = row.likelihood * row.impact;
                    row.level = levelForScore(row.score);
                    risks.add(row);
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load risk register. " + ex.getMessage());
        }

        risks.sort((a, b) -> Integer.compare(b.score, a.score));
        for (RiskRow row : risks) {
            riskModel.addRow(new Object[]{
                    row.id,
                    row.code,
                    row.description,
                    row.likelihood,
                    row.impact,
                    row.score,
                    row.level,
                    row.ownerName,
                    row.status
            });
        }
        renderMatrix(risks);
        renderRiskTree(risks);
    }

    private void renderMatrix(List<RiskRow> risks) {
        matrixPanel.removeAll();
        matrixPanel.setLayout(new GridLayout(6, 6, 3, 3));

        JLabel blank = new JLabel("", SwingConstants.CENTER);
        blank.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        matrixPanel.add(blank);
        for (int impact = 5; impact >= 1; impact--) {
            JLabel label = new JLabel("I=" + impact, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            matrixPanel.add(label);
        }

        for (int likelihood = 5; likelihood >= 1; likelihood--) {
            JLabel lbl = new JLabel("L=" + likelihood, SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            matrixPanel.add(lbl);
            for (int impact = 5; impact >= 1; impact--) {
                JPanel cell = new JPanel(new BorderLayout());
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                StringBuilder codes = new StringBuilder();
                for (RiskRow risk : risks) {
                    if (risk.likelihood == likelihood && risk.impact == impact) {
                        if (codes.length() > 0) codes.append("<br>");
                        codes.append(risk.code);
                    }
                }
                cell.setBackground(cellColorForScore(likelihood * impact));
                if (codes.length() > 0) {
                    JLabel text = new JLabel("<html>" + codes.toString().replace("\n", "<br>") + "</html>");
                    text.setHorizontalAlignment(SwingConstants.CENTER);
                    cell.add(text, BorderLayout.CENTER);
                }
                matrixPanel.add(cell);
            }
        }

        matrixPanel.revalidate();
        matrixPanel.repaint();
    }

    private Color cellColorForScore(int score) {
        if (score >= 15) return new Color(255, 180, 180);
        if (score >= 8) return new Color(255, 245, 170);
        return new Color(190, 255, 190);
    }

    private void renderRiskTree(List<RiskRow> risks) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Risks");
        risks.sort((a, b) -> Integer.compare(b.score, a.score));
        for (RiskRow risk : risks) {
            DefaultMutableTreeNode riskNode = new DefaultMutableTreeNode(risk.code + " (score " + risk.score + ")");
            riskNode.add(new DefaultMutableTreeNode("Preventive: " + risk.preventiveAction));
            riskNode.add(new DefaultMutableTreeNode("Corrective: " + risk.correctiveAction));
            root.add(riskNode);
        }
        riskTree.setModel(new DefaultTreeModel(root));
    }

    private void promptRiskDialog(boolean edit, int selectedId) {
        String currentCode = "";
        String currentDescription = "";
        int currentLikelihood = 3;
        int currentImpact = 3;
        String currentPreventive = "";
        String currentCorrective = "";
        String currentStatus = "Open";

        if (edit) {
            int row = riskTable.getSelectedRow();
            if (row < 0) {
                showError("Select a risk first.");
                return;
            }
            selectedId = Integer.parseInt(riskModel.getValueAt(row, 0).toString());
            currentCode = riskModel.getValueAt(row, 1).toString();
            currentDescription = riskModel.getValueAt(row, 2).toString();
            currentLikelihood = Integer.parseInt(riskModel.getValueAt(row, 3).toString());
            currentImpact = Integer.parseInt(riskModel.getValueAt(row, 4).toString());
            currentStatus = riskModel.getValueAt(row, 8).toString();
        }

        Map<String, Integer> memberMap = loadMemberMap();
        JTextField codeField = new JTextField(currentCode);
        JTextArea descriptionArea = new JTextArea(currentDescription, 3, 35);
        JComboBox<Integer> likelihoodCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        likelihoodCombo.setSelectedItem(currentLikelihood);
        JComboBox<Integer> impactCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        impactCombo.setSelectedItem(currentImpact);
        JTextArea preventiveArea = new JTextArea(currentPreventive, 3, 35);
        JTextArea correctiveArea = new JTextArea(currentCorrective, 3, 35);
        JComboBox<String> ownerCombo = new JComboBox<>(memberMap.keySet().toArray(new String[0]));
        ownerCombo.insertItemAt("(None)", 0);
        ownerCombo.setSelectedItem("(None)");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Open", "Mitigated", "Occurred", "Closed"});
        statusCombo.setSelectedItem(currentStatus);

        if (edit) {
            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement("SELECT preventive_action, corrective_action, owner_id FROM risk WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, selectedId);
                ps.setInt(2, PROJECT_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentPreventive = rs.getString("preventive_action") == null ? "" : rs.getString("preventive_action");
                        currentCorrective = rs.getString("corrective_action") == null ? "" : rs.getString("corrective_action");
                        int ownerId = rs.getInt("owner_id");
                        if (!rs.wasNull()) {
                            setComboSelection(ownerCombo, memberMap, ownerId);
                        }
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load risk details. " + ex.getMessage());
                return;
            }
            preventiveArea.setText(currentPreventive);
            correctiveArea.setText(currentCorrective);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));
        panel.add(new JLabel("Likelihood (1-5):"));
        panel.add(likelihoodCombo);
        panel.add(new JLabel("Impact (1-5):"));
        panel.add(impactCombo);
        panel.add(new JLabel("Preventive action:"));
        panel.add(new JScrollPane(preventiveArea));
        panel.add(new JLabel("Corrective action:"));
        panel.add(new JScrollPane(correctiveArea));
        panel.add(new JLabel("Owner:"));
        panel.add(ownerCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit risk" : "Add risk", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String code = codeField.getText().trim();
            String description = descriptionArea.getText().trim();
            Integer likelihood = (Integer) likelihoodCombo.getSelectedItem();
            Integer impact = (Integer) impactCombo.getSelectedItem();
            String preventive = preventiveArea.getText().trim();
            String corrective = correctiveArea.getText().trim();
            Integer ownerId = memberMap.get((String) ownerCombo.getSelectedItem());
            if ("(None)".equals(ownerCombo.getSelectedItem())) ownerId = null;
            String status = (String) statusCombo.getSelectedItem();

            if (code.isEmpty() || description.isEmpty() || likelihood == null || impact == null || status == null) {
                showError("Code, description, likelihood, impact and status are required.");
                return;
            }

            try (Connection c = Database.get()) {
                if (edit) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE risk SET code = ?, description = ?, likelihood = ?, impact = ?, preventive_action = ?, corrective_action = ?, owner_id = ?, status = ? WHERE id = ? AND project_id = ?")) {
                        ps.setString(1, code);
                        ps.setString(2, description);
                        ps.setInt(3, likelihood);
                        ps.setInt(4, impact);
                        ps.setString(5, preventive.isEmpty() ? null : preventive);
                        ps.setString(6, corrective.isEmpty() ? null : corrective);
                        if (ownerId != null) ps.setInt(7, ownerId); else ps.setNull(7, java.sql.Types.INTEGER);
                        ps.setString(8, status);
                        ps.setInt(9, selectedId);
                        ps.setInt(10, PROJECT_ID);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO risk (project_id, code, description, likelihood, impact, preventive_action, corrective_action, owner_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, PROJECT_ID);
                        ps.setString(2, code);
                        ps.setString(3, description);
                        ps.setInt(4, likelihood);
                        ps.setInt(5, impact);
                        ps.setString(6, preventive.isEmpty() ? null : preventive);
                        ps.setString(7, corrective.isEmpty() ? null : corrective);
                        if (ownerId != null) ps.setInt(8, ownerId); else ps.setNull(8, java.sql.Types.INTEGER);
                        ps.setString(9, status);
                        ps.executeUpdate();
                    }
                }
                loadRiskRegister();
            } catch (SQLException ex) {
                showError("Unable to save risk. " + ex.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void editSelectedRisk() {
        int row = riskTable.getSelectedRow();
        if (row < 0) {
            showError("Select a risk first.");
            return;
        }
        promptRiskDialog(true, -1);
    }

    private void deleteSelectedRisk() {
        int row = riskTable.getSelectedRow();
        if (row < 0) {
            showError("Select a risk first.");
            return;
        }
        int id = Integer.parseInt(riskModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this risk?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM risk WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadRiskRegister();
        } catch (SQLException ex) {
            showError("Unable to delete risk. " + ex.getMessage());
        }
    }

    private Map<String, Integer> loadMemberMap() {
        Map<String, Integer> map = new HashMap<>();
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

    private Map<String, Integer> loadWbsMap() {
        Map<String, Integer> map = new HashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, name FROM wbs_item WHERE project_id = ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("code") + " - " + rs.getString("name"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load WBS items. " + ex.getMessage());
        }
        return map;
    }

    private Map<String, Integer> loadActivityMap() {
        Map<String, Integer> map = new HashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, name FROM activity WHERE project_id = ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("code") + " - " + rs.getString("name"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load activities. " + ex.getMessage());
        }
        return map;
    }

    private Map<String, Integer> loadApprovedRequirementMap() {
        Map<String, Integer> map = new HashMap<>();
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
        if (selectedId <= 0) {
            combo.setSelectedItem("(None)");
            return;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(selectedId)) {
                combo.setSelectedItem(entry.getKey());
                return;
            }
        }
    }

    private String levelForScore(int score) {
        if (score >= 15) return "High";
        if (score >= 8) return "Medium";
        return "Low";
    }

    private LocalDate parseDate(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " must use yyyy-MM-dd format.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation error", JOptionPane.ERROR_MESSAGE);
    }

    private static class RiskRow {
        int id;
        String code;
        String description;
        int likelihood;
        int impact;
        int score;
        String level;
        String preventiveAction;
        String correctiveAction;
        String ownerName;
        String status;
    }
}
