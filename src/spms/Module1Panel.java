package spms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Module1Panel extends JPanel {
    private static final int PROJECT_ID = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JTextField projectNameField = new JTextField();
    private final JTextArea objectivesArea = new JTextArea(4, 40);
    private final JTextArea scopeArea = new JTextArea(4, 40);
    private final JTextField approvedBudgetField = new JTextField();
    private final JTextField startDateField = new JTextField();
    private final JTextField deadlineField = new JTextField();
    private final JComboBox<String> projectStatusCombo = new JComboBox<>(
            new String[]{"Initiated", "Planning", "Executing", "Monitoring", "Closed"});
    private final JTextField statusDateField = new JTextField();

    private final JTextArea projectPlanScopeArea = new JTextArea(4, 35);
    private final JTextArea projectPlanScheduleArea = new JTextArea(4, 35);
    private final JTextArea projectPlanCostArea = new JTextArea(4, 35);
    private final JTextArea projectPlanQualityArea = new JTextArea(4, 35);
    private final JLabel projectPlanVersionLabel = new JLabel("Version: -");
    private final JLabel projectPlanLastUpdatedLabel = new JLabel("Last updated: -");

    private final DefaultTableModel stakeholderModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Role", "Interest"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable stakeholderTable = new JTable(stakeholderModel);

    private final DefaultTableModel teamModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Role"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable teamTable = new JTable(teamModel);

    private final DefaultTableModel milestoneModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Target Date", "Status", "Responsible Member", "Linked Activity"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable milestoneTable = new JTable(milestoneModel);

    private final JLabel statusLabel = new JLabel(" ");

    public Module1Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildProjectSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildPlanSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildStakeholderSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildTeamSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildMilestoneSection());
        content.add(Box.createVerticalStrut(10));
        content.add(buildClosureSection());

        add(new JScrollPane(content), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel buildProjectSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Project Charter"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(fields, gbc, row++, "Project name:", projectNameField, 0, 3);
        addTextAreaField(fields, gbc, row++, "Objectives:", new JScrollPane(objectivesArea), 0, 3);
        addTextAreaField(fields, gbc, row++, "Scope boundaries:", new JScrollPane(scopeArea), 0, 3);
        addField(fields, gbc, row, "Approved budget:", approvedBudgetField, 0, 1);
        addField(fields, gbc, row, "Status:", projectStatusCombo, 1, 1);
        row++;
        addField(fields, gbc, row, "Start date (yyyy-MM-dd):", startDateField, 0, 1);
        addField(fields, gbc, row, "Deadline (yyyy-MM-dd):", deadlineField, 1, 1);
        row++;
        addField(fields, gbc, row++, "Status date (yyyy-MM-dd):", statusDateField, 0, 3);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save Project");
        save.addActionListener(e -> saveProject());
        actions.add(save);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPlanSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Project Plan"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(projectPlanVersionLabel);
        top.add(Box.createHorizontalStrut(20));
        top.add(projectPlanLastUpdatedLabel);

        JPanel fields = new JPanel(new GridLayout(2, 2, 10, 10));
        fields.add(new JScrollPane(projectPlanScopeArea));
        fields.add(new JScrollPane(projectPlanScheduleArea));
        fields.add(new JScrollPane(projectPlanCostArea));
        fields.add(new JScrollPane(projectPlanQualityArea));

        JPanel labels = new JPanel(new GridLayout(2, 2, 10, 10));
        labels.add(new JLabel("Scope summary"));
        labels.add(new JLabel("Schedule summary"));
        labels.add(new JLabel("Cost summary"));
        labels.add(new JLabel("Quality summary"));

        JPanel all = new JPanel(new BorderLayout(8, 8));
        all.add(top, BorderLayout.NORTH);
        all.add(labels, BorderLayout.CENTER);
        all.add(fields, BorderLayout.SOUTH);

        JButton savePlan = new JButton("Save Project Plan");
        savePlan.addActionListener(e -> saveProjectPlan());
        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        action.add(savePlan);

        panel.add(all, BorderLayout.CENTER);
        panel.add(action, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStakeholderSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Stakeholders"));
        panel.add(new JScrollPane(stakeholderTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptStakeholderDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedStakeholder());
        deleteBtn.addActionListener(e -> deleteSelectedStakeholder());
        refreshBtn.addActionListener(e -> refreshAll());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTeamSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Team Responsibilities"));
        panel.add(new JScrollPane(teamTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptTeamMemberDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedTeamMember());
        deleteBtn.addActionListener(e -> deleteSelectedTeamMember());
        refreshBtn.addActionListener(e -> refreshAll());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMilestoneSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Milestones"));
        panel.add(new JScrollPane(milestoneTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptMilestoneDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedMilestone());
        deleteBtn.addActionListener(e -> deleteSelectedMilestone());
        refreshBtn.addActionListener(e -> refreshAll());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildClosureSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close Project");
        closeBtn.addActionListener(e -> closeProject());
        panel.add(closeBtn);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field, int group, int fieldSpan) {
        addGridBagField(panel, gbc, row, label, field, group, fieldSpan);
    }

    private void addTextAreaField(JPanel panel, GridBagConstraints gbc, int row, String label, JScrollPane field, int group, int fieldSpan) {
        addGridBagField(panel, gbc, row, label, field, group, fieldSpan);
    }

    private void addGridBagField(JPanel panel, GridBagConstraints gbc, int row, String label,
                                 JComponent field, int group, int fieldSpan) {
        int labelColumn = group * 2;
        gbc.gridy = row;
        gbc.gridx = labelColumn;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(label), (GridBagConstraints) gbc.clone());

        gbc.gridx = labelColumn + 1;
        gbc.gridwidth = fieldSpan;
        gbc.weightx = 1;
        gbc.fill = field instanceof JScrollPane ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        if (field instanceof JScrollPane) {
            gbc.weighty = 1;
        }
        panel.add(field, (GridBagConstraints) gbc.clone());
        gbc.weighty = 0;
    }

    private void refreshAll() {
        loadProject();
        loadProjectPlan();
        loadStakeholders();
        loadTeamMembers();
        loadMilestones();
    }

    private void loadProject() {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM project WHERE id = ?")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectNameField.setText(rs.getString("name"));
                    objectivesArea.setText(rs.getString("objectives") == null ? "" : rs.getString("objectives"));
                    scopeArea.setText(rs.getString("scope_boundaries") == null ? "" : rs.getString("scope_boundaries"));
                    approvedBudgetField.setText(rs.getBigDecimal("approved_budget") == null ? "" : rs.getBigDecimal("approved_budget").toPlainString());
                    startDateField.setText(rs.getDate("start_date") == null ? "" : rs.getDate("start_date").toLocalDate().format(DATE_FORMAT));
                    deadlineField.setText(rs.getDate("deadline") == null ? "" : rs.getDate("deadline").toLocalDate().format(DATE_FORMAT));
                    projectStatusCombo.setSelectedItem(rs.getString("status"));
                    statusDateField.setText(rs.getDate("status_date") == null ? "" : rs.getDate("status_date").toLocalDate().format(DATE_FORMAT));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load the project details. " + ex.getMessage());
        }
    }

    private void saveProject() {
        try {
            String name = projectNameField.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Project name is required.");
            }
            String objectives = objectivesArea.getText().trim();
            String scope = scopeArea.getText().trim();
            BigDecimal budget = parseBigDecimal(approvedBudgetField.getText().trim(), "Approved budget");
            LocalDate start = parseDate(startDateField.getText().trim(), "Start date");
            LocalDate deadline = parseDate(deadlineField.getText().trim(), "Deadline");
            if (deadline.isBefore(start)) {
                throw new IllegalArgumentException("Deadline must be after the start date.");
            }
            LocalDate statusDate = parseOptionalDate(statusDateField.getText().trim(), "Status date");
            String status = (String) projectStatusCombo.getSelectedItem();
            if (status == null || status.isEmpty()) {
                throw new IllegalArgumentException("Status is required.");
            }

            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE project SET name = ?, objectives = ?, scope_boundaries = ?, approved_budget = ?, start_date = ?, deadline = ?, status = ?, status_date = ? WHERE id = ?")) {
                ps.setString(1, name);
                ps.setString(2, objectives.isEmpty() ? null : objectives);
                ps.setString(3, scope.isEmpty() ? null : scope);
                ps.setBigDecimal(4, budget);
                ps.setDate(5, java.sql.Date.valueOf(start));
                ps.setDate(6, java.sql.Date.valueOf(deadline));
                ps.setString(7, status);
                ps.setDate(8, statusDate == null ? null : java.sql.Date.valueOf(statusDate));
                ps.setInt(9, PROJECT_ID);
                ps.executeUpdate();
            }
            statusLabel.setText("Project charter updated successfully.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Unable to save the project charter. " + ex.getMessage());
        }
    }

    private void loadProjectPlan() {
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM project_plan WHERE project_id = ?")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectPlanScopeArea.setText(rs.getString("scope_summary") == null ? "" : rs.getString("scope_summary"));
                    projectPlanScheduleArea.setText(rs.getString("schedule_summary") == null ? "" : rs.getString("schedule_summary"));
                    projectPlanCostArea.setText(rs.getString("cost_summary") == null ? "" : rs.getString("cost_summary"));
                    projectPlanQualityArea.setText(rs.getString("quality_summary") == null ? "" : rs.getString("quality_summary"));
                    projectPlanVersionLabel.setText("Version: " + rs.getInt("version"));
                    projectPlanLastUpdatedLabel.setText("Last updated: " + (rs.getDate("last_updated") == null ? "-" : rs.getDate("last_updated").toLocalDate().format(DATE_FORMAT)));
                } else {
                    projectPlanScopeArea.setText("");
                    projectPlanScheduleArea.setText("");
                    projectPlanCostArea.setText("");
                    projectPlanQualityArea.setText("");
                    projectPlanVersionLabel.setText("Version: 0");
                    projectPlanLastUpdatedLabel.setText("Last updated: -");
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load the project plan. " + ex.getMessage());
        }
    }

    private void saveProjectPlan() {
        try {
            String scopeSummary = projectPlanScopeArea.getText().trim();
            String scheduleSummary = projectPlanScheduleArea.getText().trim();
            String costSummary = projectPlanCostArea.getText().trim();
            String qualitySummary = projectPlanQualityArea.getText().trim();

            if (scopeSummary.isEmpty() || scheduleSummary.isEmpty() || costSummary.isEmpty() || qualitySummary.isEmpty()) {
                throw new IllegalArgumentException("All project plan summary fields are required.");
            }

            try (Connection c = Database.get()) {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO project_plan (project_id, scope_summary, schedule_summary, cost_summary, quality_summary, version, last_updated) VALUES (?, ?, ?, ?, ?, 1, ?) ON DUPLICATE KEY UPDATE scope_summary = VALUES(scope_summary), schedule_summary = VALUES(schedule_summary), cost_summary = VALUES(cost_summary), quality_summary = VALUES(quality_summary), version = version + 1, last_updated = VALUES(last_updated)")) {
                    ps.setInt(1, PROJECT_ID);
                    ps.setString(2, scopeSummary);
                    ps.setString(3, scheduleSummary);
                    ps.setString(4, costSummary);
                    ps.setString(5, qualitySummary);
                    ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }
                c.commit();
            }
            statusLabel.setText("Project plan saved and version incremented.");
            loadProjectPlan();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Unable to save the project plan. " + ex.getMessage());
        }
    }

    private void loadStakeholders() {
        stakeholderModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM stakeholder WHERE project_id = ? ORDER BY id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stakeholderModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("role"),
                            rs.getString("interest")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load stakeholders. " + ex.getMessage());
        }
    }

    private void promptStakeholderDialog(boolean edit, int selectedId) {
        String currentName = "";
        String currentRole = "";
        String currentInterest = "";
        if (edit) {
            int row = stakeholderTable.getSelectedRow();
            if (row < 0) {
                showError("Select a stakeholder row to edit.");
                return;
            }
            selectedId = Integer.parseInt(stakeholderModel.getValueAt(row, 0).toString());
            currentName = stakeholderModel.getValueAt(row, 1).toString();
            currentRole = stakeholderModel.getValueAt(row, 2).toString();
            currentInterest = stakeholderModel.getValueAt(row, 3) == null ? "" : stakeholderModel.getValueAt(row, 3).toString();
        }

        JTextField nameField = new JTextField(currentName);
        JTextField roleField = new JTextField(currentRole);
        JTextArea interestArea = new JTextArea(currentInterest, 4, 30);

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Role:"));
        panel.add(roleField);
        panel.add(new JLabel("Interest:"));
        panel.add(new JScrollPane(interestArea));

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit stakeholder" : "Add stakeholder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String role = roleField.getText().trim();
        String interest = interestArea.getText().trim();
        if (name.isEmpty() || role.isEmpty()) {
            showError("Stakeholder name and role are required.");
            return;
        }

        try (Connection c = Database.get()) {
            if (edit) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE stakeholder SET name = ?, role = ?, interest = ? WHERE id = ? AND project_id = ?")) {
                    ps.setString(1, name);
                    ps.setString(2, role);
                    ps.setString(3, interest.isEmpty() ? null : interest);
                    ps.setInt(4, selectedId);
                    ps.setInt(5, PROJECT_ID);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO stakeholder (project_id, name, role, interest) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, PROJECT_ID);
                    ps.setString(2, name);
                    ps.setString(3, role);
                    ps.setString(4, interest.isEmpty() ? null : interest);
                    ps.executeUpdate();
                }
            }
            loadStakeholders();
            statusLabel.setText(edit ? "Stakeholder updated." : "Stakeholder added.");
        } catch (SQLException ex) {
            showError("Unable to save stakeholder. " + ex.getMessage());
        }
    }

    private void editSelectedStakeholder() {
        if (stakeholderTable.getSelectedRow() < 0) {
            showError("Select a stakeholder first.");
            return;
        }
        promptStakeholderDialog(true, -1);
    }

    private void deleteSelectedStakeholder() {
        int row = stakeholderTable.getSelectedRow();
        if (row < 0) {
            showError("Select a stakeholder first.");
            return;
        }
        int id = Integer.parseInt(stakeholderModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this stakeholder?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM stakeholder WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadStakeholders();
            statusLabel.setText("Stakeholder deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete stakeholder. " + ex.getMessage());
        }
    }

    private void loadTeamMembers() {
        teamModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM team_member WHERE project_id = ? ORDER BY id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teamModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("role")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load team members. " + ex.getMessage());
        }
    }

    private void promptTeamMemberDialog(boolean edit, int selectedId) {
        String currentName = "";
        String currentRole = "";
        if (edit) {
            int row = teamTable.getSelectedRow();
            if (row < 0) {
                showError("Select a team member row to edit.");
                return;
            }
            selectedId = Integer.parseInt(teamModel.getValueAt(row, 0).toString());
            currentName = teamModel.getValueAt(row, 1).toString();
            currentRole = teamModel.getValueAt(row, 2).toString();
        }

        JTextField nameField = new JTextField(currentName);
        JTextField roleField = new JTextField(currentRole);
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Role:"));
        panel.add(roleField);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit team member" : "Add team member", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String role = roleField.getText().trim();
        if (name.isEmpty() || role.isEmpty()) {
            showError("Team member name and role are required.");
            return;
        }

        try (Connection c = Database.get()) {
            if (edit) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE team_member SET name = ?, role = ? WHERE id = ? AND project_id = ?")) {
                    ps.setString(1, name);
                    ps.setString(2, role);
                    ps.setInt(3, selectedId);
                    ps.setInt(4, PROJECT_ID);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO team_member (project_id, name, role) VALUES (?, ?, ?)")) {
                    ps.setInt(1, PROJECT_ID);
                    ps.setString(2, name);
                    ps.setString(3, role);
                    ps.executeUpdate();
                }
            }
            loadTeamMembers();
            statusLabel.setText(edit ? "Team member updated." : "Team member added.");
        } catch (SQLException ex) {
            showError("Unable to save team member. " + ex.getMessage());
        }
    }

    private void editSelectedTeamMember() {
        if (teamTable.getSelectedRow() < 0) {
            showError("Select a team member first.");
            return;
        }
        promptTeamMemberDialog(true, -1);
    }

    private void deleteSelectedTeamMember() {
        int row = teamTable.getSelectedRow();
        if (row < 0) {
            showError("Select a team member first.");
            return;
        }
        int id = Integer.parseInt(teamModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this team member?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM team_member WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadTeamMembers();
            statusLabel.setText("Team member deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete team member. " + ex.getMessage());
        }
    }

    private void loadMilestones() {
        milestoneModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT m.id, m.name, m.target_date, m.status, tm.name AS responsible_name, a.name AS activity_name, p.status_date " +
                             "FROM milestone m " +
                             "LEFT JOIN team_member tm ON tm.id = m.responsible_id " +
                             "LEFT JOIN activity a ON a.id = m.activity_id " +
                             "LEFT JOIN project p ON p.id = ? " +
                             "WHERE m.project_id = ? ORDER BY m.id")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String displayStatus = rs.getString("status");
                    java.sql.Date targetDate = rs.getDate("target_date");
                    java.sql.Date statusDate = rs.getDate("status_date");
                    if (targetDate != null && statusDate != null && "Achieved".equals(displayStatus) == false) {
                        LocalDate target = targetDate.toLocalDate();
                        LocalDate projectStatusDate = statusDate.toLocalDate();
                        if (target.isBefore(projectStatusDate)) {
                            displayStatus = "Missed";
                        }
                    }
                    milestoneModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            targetDate == null ? "" : targetDate.toLocalDate().format(DATE_FORMAT),
                            displayStatus,
                            rs.getString("responsible_name") == null ? "" : rs.getString("responsible_name"),
                            rs.getString("activity_name") == null ? "" : rs.getString("activity_name")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load milestones. " + ex.getMessage());
        }
    }

    private void promptMilestoneDialog(boolean edit, int selectedId) {
        String currentName = "";
        String currentTargetDate = "";
        String currentStatus = "Pending";
        int selectedResponsibleId = -1;
        int selectedActivityId = -1;

        if (edit) {
            int row = milestoneTable.getSelectedRow();
            if (row < 0) {
                showError("Select a milestone row to edit.");
                return;
            }
            selectedId = Integer.parseInt(milestoneModel.getValueAt(row, 0).toString());
            currentName = milestoneModel.getValueAt(row, 1).toString();
            currentTargetDate = milestoneModel.getValueAt(row, 2).toString();
            currentStatus = milestoneModel.getValueAt(row, 3).toString();
        }

        Map<String, Integer> teamMap = loadMemberMap();
        Map<String, Integer> activityMap = loadActivityMap();

        JTextField nameField = new JTextField(currentName);
        JTextField targetField = new JTextField(currentTargetDate);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Achieved", "Missed"});
        statusCombo.setSelectedItem(currentStatus);
        JComboBox<String> responsibleCombo = new JComboBox<>(teamMap.keySet().toArray(new String[0]));
        JComboBox<String> activityCombo = new JComboBox<>(activityMap.keySet().toArray(new String[0]));

        if (edit) {
            try (Connection c = Database.get();
                 PreparedStatement ps = c.prepareStatement("SELECT responsible_id, activity_id FROM milestone WHERE id = ? AND project_id = ?")) {
                ps.setInt(1, selectedId);
                ps.setInt(2, PROJECT_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        selectedResponsibleId = rs.getInt("responsible_id");
                        selectedActivityId = rs.getInt("activity_id");
                    }
                }
            } catch (SQLException ex) {
                showError("Unable to load milestone details. " + ex.getMessage());
                return;
            }
            setComboSelection(responsibleCombo, teamMap, selectedResponsibleId);
            setComboSelection(activityCombo, activityMap, selectedActivityId);
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Target date (yyyy-MM-dd):"));
        panel.add(targetField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Responsible member:"));
        panel.add(responsibleCombo);
        panel.add(new JLabel("Linked activity:"));
        panel.add(activityCombo);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit milestone" : "Add milestone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String targetDate = targetField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        Integer responsibleId = teamMap.get((String) responsibleCombo.getSelectedItem());
        Integer activityId = activityMap.get((String) activityCombo.getSelectedItem());

        if (name.isEmpty() || targetDate.isEmpty() || responsibleId == null || activityId == null) {
            showError("Milestone name, target date, responsible member, and linked activity are required.");
            return;
        }

        LocalDate parsedTarget = parseDate(targetDate, "Target date");

        try (Connection c = Database.get()) {
            if (edit) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE milestone SET name = ?, target_date = ?, status = ?, responsible_id = ?, activity_id = ? WHERE id = ? AND project_id = ?")) {
                    ps.setString(1, name);
                    ps.setDate(2, java.sql.Date.valueOf(parsedTarget));
                    ps.setString(3, status);
                    ps.setInt(4, responsibleId);
                    ps.setInt(5, activityId);
                    ps.setInt(6, selectedId);
                    ps.setInt(7, PROJECT_ID);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO milestone (project_id, name, target_date, status, responsible_id, activity_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, PROJECT_ID);
                    ps.setString(2, name);
                    ps.setDate(3, java.sql.Date.valueOf(parsedTarget));
                    ps.setString(4, status);
                    ps.setInt(5, responsibleId);
                    ps.setInt(6, activityId);
                    ps.executeUpdate();
                }
            }
            loadMilestones();
            statusLabel.setText(edit ? "Milestone updated." : "Milestone added.");
        } catch (SQLException ex) {
            showError("Unable to save milestone. " + ex.getMessage());
        }
    }

    private void editSelectedMilestone() {
        if (milestoneTable.getSelectedRow() < 0) {
            showError("Select a milestone first.");
            return;
        }
        promptMilestoneDialog(true, -1);
    }

    private void deleteSelectedMilestone() {
        int row = milestoneTable.getSelectedRow();
        if (row < 0) {
            showError("Select a milestone first.");
            return;
        }
        int id = Integer.parseInt(milestoneModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this milestone?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM milestone WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadMilestones();
            statusLabel.setText("Milestone deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete milestone. " + ex.getMessage());
        }
    }

    private void closeProject() {
        String closureNotes = JOptionPane.showInputDialog(this, "Enter closure notes:", "Close project", JOptionPane.PLAIN_MESSAGE);
        if (closureNotes == null) {
            return;
        }
        if (closureNotes.trim().isEmpty()) {
            showError("Closure notes are required before closing the project.");
            return;
        }

        StringBuilder blockedReasons = new StringBuilder();
        try (Connection c = Database.get()) {
            if (!hasAllActivitiesCompleted(c)) {
                blockedReasons.append("- At least one activity is not completed.\n");
            }
            if (hasBlockingDefects(c)) {
                blockedReasons.append("- A Critical or High defect is still Open/In Progress.\n");
            }
            if (hasPendingChangeRequests(c)) {
                blockedReasons.append("- A change request is still Pending.\n");
            }

            if (blockedReasons.length() > 0) {
                JOptionPane.showMessageDialog(this, "Project closure is blocked:\n\n" + blockedReasons, "Closure blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }

            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE project SET status = 'Closed', status_date = ?, closure_date = ?, closure_notes = ? WHERE id = ?")) {
                LocalDate today = LocalDate.now();
                ps.setDate(1, java.sql.Date.valueOf(today));
                ps.setDate(2, java.sql.Date.valueOf(today));
                ps.setString(3, closureNotes.trim());
                ps.setInt(4, PROJECT_ID);
                ps.executeUpdate();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
            loadProject();
            statusLabel.setText("Project closed successfully.");
        } catch (SQLException ex) {
            showError("Unable to close the project. " + ex.getMessage());
        }
    }

    private boolean hasAllActivitiesCompleted(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM activity WHERE project_id = ? AND status <> 'Completed'")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private boolean hasBlockingDefects(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM defect WHERE project_id = ? AND severity IN ('Critical', 'High') AND status NOT IN ('Resolved', 'Closed')")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasPendingChangeRequests(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM change_request WHERE project_id = ? AND status = 'Pending'")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
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
            showError("Unable to load team members for milestone selection. " + ex.getMessage());
        }
        return map;
    }

    private Map<String, Integer> loadActivityMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM activity WHERE project_id = ? ORDER BY name")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("name"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load activities for milestone selection. " + ex.getMessage());
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

    private BigDecimal parseBigDecimal(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            BigDecimal parsed = new BigDecimal(value.trim());
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(label + " cannot be negative.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private LocalDate parseDate(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use the format yyyy-MM-dd.");
        }
    }

    private LocalDate parseOptionalDate(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " must use the format yyyy-MM-dd.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText(message);
    }
}
