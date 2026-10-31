package spms;

import javax.swing.*;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Module2Panel extends JPanel {
    private static final int PROJECT_ID = 1;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DefaultTableModel requirementModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Title", "Description", "Priority", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable requirementTable = new JTable(requirementModel);

    private final DefaultTableModel changeRequestModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Title", "Justification", "Requirement", "Cost Impact", "Schedule Impact Days", "Status", "Requested Date", "Decision Date", "Applied"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable changeRequestTable = new JTable(changeRequestModel);

    private final DefaultTreeModel wbsTreeModel;
    private final JTree wbsTree;
    private final DefaultListModel<String> approvedScopeModel = new DefaultListModel<>();
    private final JList<String> approvedScopeList = new JList<>(approvedScopeModel);

    public Module2Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("WBS");
        wbsTreeModel = new DefaultTreeModel(root);
        wbsTree = new JTree(wbsTreeModel);
        wbsTree.setShowsRootHandles(true);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildRequirementsPanel(),
                buildWbsAndScopePanel());
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        loadAll();
    }

    private JPanel buildRequirementsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Requirements"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> promptRequirementDialog(false, -1));
        editBtn.addActionListener(e -> editSelectedRequirement());
        deleteBtn.addActionListener(e -> deleteSelectedRequirement());
        approveBtn.addActionListener(e -> approveRequirement());
        rejectBtn.addActionListener(e -> rejectRequirement());
        refreshBtn.addActionListener(e -> loadAll());

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(approveBtn);
        buttons.add(rejectBtn);
        buttons.add(refreshBtn);
        panel.add(new JScrollPane(requirementTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWbsAndScopePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel wbsPanel = new JPanel(new BorderLayout(8, 8));
        wbsPanel.setBorder(BorderFactory.createTitledBorder("WBS Hierarchy"));

        JPanel wbsButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addChildBtn = new JButton("Add Child");
        JButton renameBtn = new JButton("Rename");
        JButton deleteBtn = new JButton("Delete");
        JButton linkBtn = new JButton("Link Requirement");
        JButton refreshBtn = new JButton("Refresh");

        addChildBtn.addActionListener(e -> addChildWbsItem());
        renameBtn.addActionListener(e -> renameSelectedWbsItem());
        deleteBtn.addActionListener(e -> deleteSelectedWbsItem());
        linkBtn.addActionListener(e -> linkRequirementToWbs());
        refreshBtn.addActionListener(e -> loadWbsTree());

        wbsButtons.add(addChildBtn);
        wbsButtons.add(renameBtn);
        wbsButtons.add(deleteBtn);
        wbsButtons.add(linkBtn);
        wbsButtons.add(refreshBtn);

        wbsPanel.add(new JScrollPane(wbsTree), BorderLayout.CENTER);
        wbsPanel.add(wbsButtons, BorderLayout.SOUTH);

        JPanel changePanel = new JPanel(new BorderLayout(8, 8));
        changePanel.setBorder(BorderFactory.createTitledBorder("Change Requests"));

        JPanel changeButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newBtn = new JButton("New");
        JButton approveChangeBtn = new JButton("Approve");
        JButton rejectChangeBtn = new JButton("Reject");
        JButton refreshChangeBtn = new JButton("Refresh");

        newBtn.addActionListener(e -> promptChangeRequestDialog(false, -1));
        approveChangeBtn.addActionListener(e -> approveChangeRequest());
        rejectChangeBtn.addActionListener(e -> rejectChangeRequest());
        refreshChangeBtn.addActionListener(e -> loadChangeRequests());

        changeButtons.add(newBtn);
        changeButtons.add(approveChangeBtn);
        changeButtons.add(rejectChangeBtn);
        changeButtons.add(refreshChangeBtn);

        changePanel.add(new JScrollPane(changeRequestTable), BorderLayout.CENTER);
        changePanel.add(changeButtons, BorderLayout.SOUTH);

        JPanel approvedScopePanel = new JPanel(new BorderLayout(8, 8));
        approvedScopePanel.setBorder(BorderFactory.createTitledBorder("Approved Scope"));
        approvedScopePanel.add(new JScrollPane(approvedScopeList), BorderLayout.CENTER);

        JPanel top = new JPanel(new GridLayout(2, 1, 8, 8));
        top.add(wbsPanel);
        top.add(changePanel);

        panel.add(top, BorderLayout.CENTER);
        panel.add(approvedScopePanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAll() {
        loadRequirements();
        loadWbsTree();
        loadApprovedScope();
        loadChangeRequests();
    }

    private void loadRequirements() {
        requirementModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM requirement WHERE project_id = ? ORDER BY status, priority, id")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requirementModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("priority"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load requirements. " + ex.getMessage());
        }
    }

    private void editSelectedRequirement() {
        int row = requirementTable.getSelectedRow();
        if (row < 0) {
            showError("Select a requirement first.");
            return;
        }
        promptRequirementDialog(true, Integer.parseInt(requirementModel.getValueAt(row, 0).toString()));
    }

    private void deleteSelectedRequirement() {
        int row = requirementTable.getSelectedRow();
        if (row < 0) {
            showError("Select a requirement first.");
            return;
        }
        int id = Integer.parseInt(requirementModel.getValueAt(row, 0).toString());
        String code = requirementModel.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Delete requirement " + code + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM requirement WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadRequirements();
            loadApprovedScope();
            statusMessage("Requirement deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete requirement. " + ex.getMessage());
        }
    }

    private void promptRequirementDialog(boolean edit, int selectedId) {
        String currentCode = "";
        String currentTitle = "";
        String currentDesc = "";
        String currentPriority = "Medium";
        String currentStatus = "Proposed";

        if (edit) {
            int row = requirementTable.getSelectedRow();
            if (row < 0) {
                showError("Select a requirement to edit.");
                return;
            }
            selectedId = Integer.parseInt(requirementModel.getValueAt(row, 0).toString());
            currentCode = requirementModel.getValueAt(row, 1).toString();
            currentTitle = requirementModel.getValueAt(row, 2).toString();
            currentDesc = requirementModel.getValueAt(row, 3) == null ? "" : requirementModel.getValueAt(row, 3).toString();
            currentPriority = requirementModel.getValueAt(row, 4).toString();
            currentStatus = requirementModel.getValueAt(row, 5).toString();
        }

        JTextField codeField = new JTextField(currentCode);
        JTextField titleField = new JTextField(currentTitle);
        JTextArea descArea = new JTextArea(currentDesc, 4, 35);
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"Critical", "High", "Medium", "Low"});
        priorityCombo.setSelectedItem(currentPriority);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Proposed", "Approved", "Rejected"});
        statusCombo.setSelectedItem(currentStatus);

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descArea));
        panel.add(new JLabel("Priority:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit requirement" : "Add requirement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        String description = descArea.getText().trim();
        String priority = (String) priorityCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        if (code.isEmpty() || title.isEmpty() || priority == null || status == null) {
            showError("Requirement code, title, priority and status are required.");
            return;
        }

        if (!"Critical".equals(priority) && !"High".equals(priority) && !"Medium".equals(priority) && !"Low".equals(priority)) {
            showError("Priority must be one of Critical, High, Medium or Low.");
            return;
        }
        if (!"Proposed".equals(status) && !"Approved".equals(status) && !"Rejected".equals(status)) {
            showError("Status must be Proposed, Approved or Rejected.");
            return;
        }

        try (Connection c = Database.get()) {
            if (edit) {
                try (PreparedStatement ps = c.prepareStatement("UPDATE requirement SET code = ?, title = ?, description = ?, priority = ?, status = ? WHERE id = ? AND project_id = ?")) {
                    ps.setString(1, code);
                    ps.setString(2, title);
                    ps.setString(3, description.isEmpty() ? null : description);
                    ps.setString(4, priority);
                    ps.setString(5, status);
                    ps.setInt(6, selectedId);
                    ps.setInt(7, PROJECT_ID);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO requirement (project_id, code, title, description, priority, status) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, PROJECT_ID);
                    ps.setString(2, code);
                    ps.setString(3, title);
                    ps.setString(4, description.isEmpty() ? null : description);
                    ps.setString(5, priority);
                    ps.setString(6, status);
                    ps.executeUpdate();
                }
            }
            loadRequirements();
            loadApprovedScope();
            statusMessage(edit ? "Requirement updated." : "Requirement added.");
        } catch (SQLException ex) {
            showError("Unable to save requirement. " + ex.getMessage());
        }
    }

    private void approveRequirement() {
        int row = requirementTable.getSelectedRow();
        if (row < 0) {
            showError("Select a requirement first.");
            return;
        }
        int id = Integer.parseInt(requirementModel.getValueAt(row, 0).toString());
        String code = requirementModel.getValueAt(row, 1).toString();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE requirement SET status = 'Approved' WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadRequirements();
            loadApprovedScope();
            statusMessage("Requirement " + code + " approved.");
        } catch (SQLException ex) {
            showError("Unable to approve requirement. " + ex.getMessage());
        }
    }

    private void rejectRequirement() {
        int row = requirementTable.getSelectedRow();
        if (row < 0) {
            showError("Select a requirement first.");
            return;
        }
        int id = Integer.parseInt(requirementModel.getValueAt(row, 0).toString());
        String code = requirementModel.getValueAt(row, 1).toString();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE requirement SET status = 'Rejected' WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadRequirements();
            loadApprovedScope();
            statusMessage("Requirement " + code + " rejected.");
        } catch (SQLException ex) {
            showError("Unable to reject requirement. " + ex.getMessage());
        }
    }

    private void loadWbsTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("WBS");
        Map<Integer, DefaultMutableTreeNode> nodes = new HashMap<>();

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM wbs_item WHERE project_id = ? ORDER BY parent_id IS NULL DESC, parent_id, code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String code = rs.getString("code");
                    String name = rs.getString("name");
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(code + " - " + name + " [" + id + "]");
                    nodes.put(id, node);
                    Integer parentId = (Integer) rs.getObject("parent_id");
                    if (parentId == null) {
                        root.add(node);
                    } else {
                        DefaultMutableTreeNode parentNode = nodes.get(parentId);
                        if (parentNode != null) {
                            parentNode.add(node);
                        } else {
                            root.add(node);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load WBS tree. " + ex.getMessage());
            return;
        }

        wbsTreeModel.setRoot(root);
        wbsTreeModel.reload();
        wbsTree.expandRow(0);
    }

    private void addChildWbsItem() {
        String name = JOptionPane.showInputDialog(this, "Enter WBS item name:", "Add child WBS item", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) wbsTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            showError("Select a parent WBS item first.");
            return;
        }

        String parentCode = selectedNode.toString();
        if (parentCode.equals("WBS")) {
            showError("Select a parent item to add a child under it.");
            return;
        }

        int parentId = getWbsIdFromNode(selectedNode);
        String code = JOptionPane.showInputDialog(this, "Enter WBS code (for example 2.5):", "Add child WBS item", JOptionPane.PLAIN_MESSAGE);
        if (code == null || code.trim().isEmpty()) {
            return;
        }
        String itemType = JOptionPane.showInputDialog(this, "Enter item type (Deliverable or Work Package):", "Add child WBS item", JOptionPane.PLAIN_MESSAGE);
        if (itemType == null || itemType.trim().isEmpty()) {
            return;
        }

        if (!"Deliverable".equals(itemType) && !"Work Package".equals(itemType)) {
            showError("Item type must be either Deliverable or Work Package.");
            return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO wbs_item (project_id, parent_id, code, name, item_type) VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, parentId);
            ps.setString(3, code.trim());
            ps.setString(4, name.trim());
            ps.setString(5, itemType.trim());
            ps.executeUpdate();
            loadWbsTree();
            statusMessage("WBS child item added.");
        } catch (SQLException ex) {
            showError("Unable to add WBS item. " + ex.getMessage());
        }
    }

    private void renameSelectedWbsItem() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) wbsTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            showError("Select a WBS item first.");
            return;
        }
        String oldValue = selectedNode.toString();
        if ("WBS".equals(oldValue)) {
            showError("The root node cannot be renamed.");
            return;
        }
        int wbsId = getWbsIdFromNode(selectedNode);
        String newName = JOptionPane.showInputDialog(this, "Rename WBS item:", oldValue, JOptionPane.PLAIN_MESSAGE);
        if (newName == null || newName.trim().isEmpty()) {
            return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE wbs_item SET name = ? WHERE id = ? AND project_id = ?")) {
            ps.setString(1, newName.trim());
            ps.setInt(2, wbsId);
            ps.setInt(3, PROJECT_ID);
            ps.executeUpdate();
            loadWbsTree();
            statusMessage("WBS item renamed.");
        } catch (SQLException ex) {
            showError("Unable to rename WBS item. " + ex.getMessage());
        }
    }

    private void deleteSelectedWbsItem() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) wbsTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            showError("Select a WBS item first.");
            return;
        }
        if ("WBS".equals(selectedNode.toString())) {
            showError("The WBS root cannot be deleted.");
            return;
        }

        int wbsId = getWbsIdFromNode(selectedNode);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this WBS item and its children?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM wbs_item WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, wbsId);
            ps.setInt(2, PROJECT_ID);
            ps.executeUpdate();
            loadWbsTree();
            statusMessage("WBS item deleted.");
        } catch (SQLException ex) {
            showError("Unable to delete WBS item. " + ex.getMessage());
        }
    }

    private void linkRequirementToWbs() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) wbsTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            showError("Select a WBS item first.");
            return;
        }
        if ("WBS".equals(selectedNode.toString())) {
            showError("Select a real WBS item to link a requirement.");
            return;
        }

        int wbsId = getWbsIdFromNode(selectedNode);
        Map<String, Integer> approvedRequirements = loadApprovedRequirementMap();
        if (approvedRequirements.isEmpty()) {
            showError("There are no approved requirements available to link.");
            return;
        }

        JComboBox<String> requirementCombo = new JComboBox<>(approvedRequirements.keySet().toArray(new String[0]));
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Approved requirement:"));
        panel.add(requirementCombo);

        int option = JOptionPane.showConfirmDialog(this, panel, "Link requirement to WBS item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String selectedLabel = (String) requirementCombo.getSelectedItem();
        Integer requirementId = approvedRequirements.get(selectedLabel);
        if (requirementId == null) {
            showError("No approved requirement selected.");
            return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE wbs_item SET requirement_id = ? WHERE id = ? AND project_id = ?")) {
            ps.setInt(1, requirementId);
            ps.setInt(2, wbsId);
            ps.setInt(3, PROJECT_ID);
            ps.executeUpdate();
            loadWbsTree();
            loadApprovedScope();
            statusMessage("Requirement linked to WBS item.");
        } catch (SQLException ex) {
            showError("Unable to link requirement to WBS item. " + ex.getMessage());
        }
    }

    private void loadApprovedScope() {
        approvedScopeModel.clear();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.code AS req_code, r.title AS req_title, w.code AS wbs_code, w.name AS wbs_name " +
                             "FROM requirement r LEFT JOIN wbs_item w ON w.requirement_id = r.id AND w.project_id = ? " +
                             "WHERE r.project_id = ? AND r.status = 'Approved' ORDER BY r.id, w.id")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String reqCode = rs.getString("req_code");
                    String reqTitle = rs.getString("req_title");
                    String wbsCode = rs.getString("wbs_code");
                    String wbsName = rs.getString("wbs_name");
                    if (wbsCode == null || wbsName == null) {
                        approvedScopeModel.addElement(reqCode + " - " + reqTitle + " (no linked WBS item)");
                    } else {
                        approvedScopeModel.addElement(reqCode + " - " + reqTitle + " -> " + wbsCode + " - " + wbsName);
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load Approved Scope. " + ex.getMessage());
        }
    }

    private Map<String, Integer> loadApprovedRequirementMap() {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, title FROM requirement WHERE project_id = ? AND status = 'Approved' ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("code") + " - " + rs.getString("title"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load approved requirement list. " + ex.getMessage());
        }
        return result;
    }

    private void loadChangeRequests() {
        changeRequestModel.setRowCount(0);
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT cr.id, cr.code, cr.title, cr.justification, r.code AS req_code, cr.cost_impact, cr.schedule_impact_days, cr.status, cr.requested_date, cr.decision_date, cr.applied " +
                             "FROM change_request cr LEFT JOIN requirement r ON r.id = cr.requirement_id AND r.project_id = ? " +
                             "WHERE cr.project_id = ? ORDER BY cr.id")) {
            ps.setInt(1, PROJECT_ID);
            ps.setInt(2, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    changeRequestModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("justification"),
                            rs.getString("req_code"),
                            rs.getBigDecimal("cost_impact"),
                            rs.getInt("schedule_impact_days"),
                            rs.getString("status"),
                            rs.getDate("requested_date") == null ? "" : rs.getDate("requested_date").toLocalDate().format(DATE_FORMAT),
                            rs.getDate("decision_date") == null ? "" : rs.getDate("decision_date").toLocalDate().format(DATE_FORMAT),
                            rs.getBoolean("applied")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load change requests. " + ex.getMessage());
        }
    }

    private void promptChangeRequestDialog(boolean edit, int selectedId) {
        String currentCode = "";
        String currentTitle = "";
        String currentJustification = "";
        String currentRequirement = "";
        BigDecimal currentCostImpact = BigDecimal.ZERO;
        int currentScheduleImpact = 0;

        if (edit) {
            int row = changeRequestTable.getSelectedRow();
            if (row < 0) {
                showError("Select a change request first.");
                return;
            }
            selectedId = Integer.parseInt(changeRequestModel.getValueAt(row, 0).toString());
            currentCode = changeRequestModel.getValueAt(row, 1).toString();
            currentTitle = changeRequestModel.getValueAt(row, 2).toString();
            currentJustification = changeRequestModel.getValueAt(row, 3) == null ? "" : changeRequestModel.getValueAt(row, 3).toString();
            currentRequirement = changeRequestModel.getValueAt(row, 4) == null ? "" : changeRequestModel.getValueAt(row, 4).toString();
            currentCostImpact = changeRequestModel.getValueAt(row, 5) == null ? BigDecimal.ZERO : new BigDecimal(changeRequestModel.getValueAt(row, 5).toString());
            currentScheduleImpact = changeRequestModel.getValueAt(row, 6) == null ? 0 : Integer.parseInt(changeRequestModel.getValueAt(row, 6).toString());
        }

        Map<String, Integer> requirementMapping = new LinkedHashMap<>();
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, code, title FROM requirement WHERE project_id = ? ORDER BY code")) {
            ps.setInt(1, PROJECT_ID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requirementMapping.put(rs.getString("code") + " - " + rs.getString("title"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            showError("Unable to load requirements for change request. " + ex.getMessage());
            return;
        }

        JTextField codeField = new JTextField(currentCode);
        JTextField titleField = new JTextField(currentTitle);
        JTextArea justificationArea = new JTextArea(currentJustification, 4, 35);
        JTextField costField = new JTextField(currentCostImpact.toPlainString());
        JTextField scheduleDaysField = new JTextField(String.valueOf(currentScheduleImpact));
        JComboBox<String> requirementCombo = new JComboBox<>(requirementMapping.keySet().toArray(new String[0]));
        if (!currentRequirement.isEmpty()) {
            String selectedRequirementKey = null;
            for (String key : requirementMapping.keySet()) {
                if (key.startsWith(currentRequirement)) {
                    selectedRequirementKey = key;
                    break;
                }
            }
            if (selectedRequirementKey != null) {
                requirementCombo.setSelectedItem(selectedRequirementKey);
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Justification:"));
        panel.add(new JScrollPane(justificationArea));
        panel.add(new JLabel("Linked requirement:"));
        panel.add(requirementCombo);
        panel.add(new JLabel("Cost impact:"));
        panel.add(costField);
        panel.add(new JLabel("Schedule impact days:"));
        panel.add(scheduleDaysField);

        int option = JOptionPane.showConfirmDialog(this, panel, edit ? "Edit change request" : "New change request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String justification = justificationArea.getText().trim();
            String requirementLabel = (String) requirementCombo.getSelectedItem();
            Integer requirementId = requirementMapping.get(requirementLabel);
            BigDecimal costImpact = parseBigDecimal(costField.getText().trim(), "Cost impact");
            int scheduleImpactDays = parseInteger(scheduleDaysField.getText().trim(), "Schedule impact days");

            if (code.isEmpty() || title.isEmpty() || justification.isEmpty() || requirementId == null) {
                showError("Code, title, justification, requirement, and cost details are required.");
                return;
            }
            if (scheduleImpactDays < 0) {
                showError("Schedule impact days cannot be negative.");
                return;
            }

            try (Connection c = Database.get()) {
                if (edit) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE change_request SET code = ?, title = ?, justification = ?, requirement_id = ?, cost_impact = ?, schedule_impact_days = ? WHERE id = ? AND project_id = ?")) {
                        ps.setString(1, code);
                        ps.setString(2, title);
                        ps.setString(3, justification);
                        ps.setInt(4, requirementId);
                        ps.setBigDecimal(5, costImpact);
                        ps.setInt(6, scheduleImpactDays);
                        ps.setInt(7, selectedId);
                        ps.setInt(8, PROJECT_ID);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO change_request (project_id, code, title, justification, requirement_id, cost_impact, schedule_impact_days, status, requested_date, applied) VALUES (?, ?, ?, ?, ?, ?, ?, 'Pending', ?, 0)")) {
                        ps.setInt(1, PROJECT_ID);
                        ps.setString(2, code);
                        ps.setString(3, title);
                        ps.setString(4, justification);
                        ps.setInt(5, requirementId);
                        ps.setBigDecimal(6, costImpact);
                        ps.setInt(7, scheduleImpactDays);
                        ps.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
                        ps.executeUpdate();
                    }
                }
                loadChangeRequests();
                statusMessage(edit ? "Change request updated." : "Change request created.");
            } catch (SQLException ex) {
                showError("Unable to save change request. " + ex.getMessage());
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }

    }

    private void approveChangeRequest() {
        int row = changeRequestTable.getSelectedRow();
        if (row < 0) {
            showError("Select a change request first.");
            return;
        }

        int changeRequestId = Integer.parseInt(changeRequestModel.getValueAt(row, 0).toString());
        String status = changeRequestModel.getValueAt(row, 7).toString();
        boolean applied = Boolean.parseBoolean(changeRequestModel.getValueAt(row, 10).toString());
        if (!"Pending".equals(status)) {
            showError("Only pending change requests can be approved.");
            return;
        }
        if (applied) {
            showError("This change request was already applied and cannot be approved again.");
            return;
        }

        try (Connection c = Database.get()) {
            c.setAutoCommit(false);
            int requirementId = -1;
            BigDecimal costImpact = BigDecimal.ZERO;
            int scheduleImpactDays = 0;
            String requirementCode = null;
            BigDecimal oldBudget = BigDecimal.ZERO;
            java.sql.Date oldDeadline = null;

            try (PreparedStatement selectPs = c.prepareStatement(
                    "SELECT cr.requirement_id, cr.cost_impact, cr.schedule_impact_days, r.code, p.approved_budget, p.deadline " +
                            "FROM change_request cr LEFT JOIN requirement r ON r.id = cr.requirement_id LEFT JOIN project p ON p.id = ? " +
                            "WHERE cr.id = ? AND cr.project_id = ?")) {
                selectPs.setInt(1, PROJECT_ID);
                selectPs.setInt(2, changeRequestId);
                selectPs.setInt(3, PROJECT_ID);
                try (ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        requirementId = rs.getInt("requirement_id");
                        costImpact = rs.getBigDecimal("cost_impact");
                        scheduleImpactDays = rs.getInt("schedule_impact_days");
                        requirementCode = rs.getString("code");
                        oldBudget = rs.getBigDecimal("approved_budget");
                        oldDeadline = rs.getDate("deadline");
                    }
                }
            }

            if (requirementId <= 0) {
                c.rollback();
                showError("The linked requirement is missing.");
                return;
            }

            LocalDate today = LocalDate.now();
            try (PreparedStatement updateRequestPs = c.prepareStatement(
                    "UPDATE change_request SET status = 'Approved', decision_date = ?, applied = 1 WHERE id = ? AND project_id = ?")) {
                updateRequestPs.setDate(1, java.sql.Date.valueOf(today));
                updateRequestPs.setInt(2, changeRequestId);
                updateRequestPs.setInt(3, PROJECT_ID);
                updateRequestPs.executeUpdate();
            }

            try (PreparedStatement updateRequirementPs = c.prepareStatement("UPDATE requirement SET status = 'Approved' WHERE id = ? AND project_id = ?")) {
                updateRequirementPs.setInt(1, requirementId);
                updateRequirementPs.setInt(2, PROJECT_ID);
                updateRequirementPs.executeUpdate();
            }

            BigDecimal newBudget = oldBudget.add(costImpact);
            LocalDate oldDeadlineDate = oldDeadline == null ? LocalDate.now() : oldDeadline.toLocalDate();
            LocalDate newDeadline = oldDeadlineDate.plusDays(scheduleImpactDays);

            try (PreparedStatement updateProjectPs = c.prepareStatement("UPDATE project SET approved_budget = ?, deadline = ? WHERE id = ?")) {
                updateProjectPs.setBigDecimal(1, newBudget);
                updateProjectPs.setDate(2, java.sql.Date.valueOf(newDeadline));
                updateProjectPs.setInt(3, PROJECT_ID);
                updateProjectPs.executeUpdate();
            }

            try (PreparedStatement updatePlanPs = c.prepareStatement("UPDATE project_plan SET version = version + 1, last_updated = ? WHERE project_id = ?")) {
                updatePlanPs.setDate(1, java.sql.Date.valueOf(today));
                updatePlanPs.setInt(2, PROJECT_ID);
                updatePlanPs.executeUpdate();
            }

            c.commit();

            JOptionPane.showMessageDialog(this,
                    "Approved change request " + requirementCode + "\n" +
                            "Old budget: PKR " + oldBudget + "\n" +
                            "New budget: PKR " + newBudget + "\n" +
                            "Old deadline: " + oldDeadlineDate + "\n" +
                            "New deadline: " + newDeadline,
                    "Change request approved", JOptionPane.INFORMATION_MESSAGE);

            loadRequirements();
            loadChangeRequests();
            loadApprovedScope();
            statusMessage("Change request approved and applied.");
        } catch (SQLException ex) {
            try (Connection rollbackConn = Database.get()) {
                rollbackConn.rollback();
            } catch (SQLException ignored) {
            }
            showError("Unable to approve the change request. " + ex.getMessage());
        }
    }

    private void rejectChangeRequest() {
        int row = changeRequestTable.getSelectedRow();
        if (row < 0) {
            showError("Select a change request first.");
            return;
        }
        int changeRequestId = Integer.parseInt(changeRequestModel.getValueAt(row, 0).toString());
        String status = changeRequestModel.getValueAt(row, 7).toString();
        if (!"Pending".equals(status)) {
            showError("Only pending change requests can be rejected.");
            return;
        }

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement("UPDATE change_request SET status = 'Rejected', decision_date = ? WHERE id = ? AND project_id = ?")) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            ps.setInt(2, changeRequestId);
            ps.setInt(3, PROJECT_ID);
            ps.executeUpdate();
            loadChangeRequests();
            statusMessage("Change request rejected.");
        } catch (SQLException ex) {
            showError("Unable to reject change request. " + ex.getMessage());
        }
    }

    private int parseInteger(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a valid whole number.");
        }
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

    private int getWbsIdFromNode(DefaultMutableTreeNode node) {
        Object idObject = node.getUserObject();
        if (idObject == null) {
            return -1;
        }
        String text = idObject.toString();
        int start = text.lastIndexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            try {
                return Integer.parseInt(text.substring(start + 1, end));
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private void statusMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Status", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation error", JOptionPane.ERROR_MESSAGE);
    }
}
