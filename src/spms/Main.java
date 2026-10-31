package spms;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main extends JFrame {

    private static final Color NAVY = new Color(22, 38, 63);
    private static final Color ACCENT = new Color(20, 145, 137);
    private static final Color ACCENT_DARK = new Color(13, 118, 112);
    private static final Color CANVAS = new Color(242, 246, 250);
    private static final Color SURFACE = Color.WHITE;
    private static final Color MUTED = new Color(113, 128, 150);
    private static final Font APP_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageCards = new JPanel(pageLayout);
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private final JTable table = new JTable();
    private final JLabel databaseStatus = new JLabel("Checking database...");
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageDescription = new JLabel();

    public Main() {
        super("Smart Healthcare Project Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1360, 850);
        setLocationRelativeTo(null);

        add(buildNavigation(), BorderLayout.WEST);
        add(buildWorkspace(), BorderLayout.CENTER);

        addPage("overview", "Project dashboard", "A live overview of delivery, quality, risk, and project health.", new Module6Panel());
        addPage("initiation", "Initiation & integration", "Project charter, stakeholders, team, milestones, and delivery plan.", new Module1Panel());
        addPage("scope", "Scope & work breakdown", "Manage requirements, the work breakdown structure, and change control.", new Module2Panel());
        addPage("schedule", "Schedule management", "Plan activities, dependencies, resources, and schedule progress.", new Module3Panel());
        addPage("cost", "Cost & earned value", "Review the approved budget, actuals, and earned-value indicators.", new Module4Panel());
        addPage("quality", "Quality & risk", "Track quality inspections, defects, risk responses, and priorities.", new Module5Panel());
        addPage("setup", "Database setup", "Check database connectivity and inspect the project tables.", buildSetupPanel());

        showPage("overview");
        refresh();
    }

    private JPanel buildNavigation() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(246, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(22, 16, 18, 16));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("SH");
        mark.setOpaque(true);
        mark.setBackground(ACCENT);
        mark.setForeground(Color.WHITE);
        mark.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(42, 42));
        JLabel name = new JLabel("CarePlan");
        name.setForeground(Color.WHITE);
        name.setFont(APP_FONT.deriveFont(Font.BOLD, 20f));
        brand.add(mark);
        brand.add(name);
        top.add(brand);

        JLabel workspace = new JLabel("PROJECT OPERATIONS");
        workspace.setForeground(new Color(157, 176, 199));
        workspace.setFont(APP_FONT.deriveFont(Font.BOLD, 10f));
        workspace.setBorder(new EmptyBorder(25, 4, 10, 0));
        top.add(workspace);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        addNavigationButton(menu, "overview", "00", "Overview");
        addNavigationButton(menu, "initiation", "01", "Initiation");
        addNavigationButton(menu, "scope", "02", "Scope & WBS");
        addNavigationButton(menu, "schedule", "03", "Schedule");
        addNavigationButton(menu, "cost", "04", "Cost & EVM");
        addNavigationButton(menu, "quality", "05", "Quality & Risk");

        JLabel administration = new JLabel("ADMINISTRATION");
        administration.setForeground(new Color(157, 176, 199));
        administration.setFont(APP_FONT.deriveFont(Font.BOLD, 10f));
        administration.setBorder(new EmptyBorder(24, 4, 10, 0));
        menu.add(administration);
        addNavigationButton(menu, "setup", "DB", "Database setup");
        sidebar.add(menu, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        JLabel product = new JLabel("SMART HEALTHCARE PMS");
        product.setForeground(new Color(157, 176, 199));
        product.setFont(APP_FONT.deriveFont(Font.BOLD, 10f));
        footer.add(product);
        JLabel version = new JLabel("Project workspace  |  v1.0");
        version.setForeground(new Color(125, 146, 171));
        version.setFont(APP_FONT.deriveFont(11f));
        version.setBorder(new EmptyBorder(5, 0, 0, 0));
        footer.add(version);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavigationButton(JPanel menu, String key, String number, String text) {
        JButton button = new NavigationButton(number + "     " + text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(APP_FONT.deriveFont(Font.PLAIN, 13f));
        button.setForeground(new Color(225, 234, 245));
        button.setBackground(NAVY);
        button.setBorder(new EmptyBorder(12, 12, 12, 10));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> showPage(key));
        navigationButtons.put(key, button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(4));
    }

    private JPanel buildWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(0, 18));
        workspace.setBackground(CANVAS);
        workspace.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        pageTitle.setFont(APP_FONT.deriveFont(Font.BOLD, 25f));
        pageTitle.setForeground(NAVY);
        pageDescription.setFont(APP_FONT.deriveFont(13f));
        pageDescription.setForeground(MUTED);
        pageDescription.setBorder(new EmptyBorder(5, 0, 0, 0));
        titles.add(pageTitle);
        titles.add(pageDescription);
        header.add(titles, BorderLayout.WEST);

        databaseStatus.setOpaque(true);
        databaseStatus.setBackground(new Color(226, 244, 240));
        databaseStatus.setForeground(new Color(20, 112, 95));
        databaseStatus.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
        databaseStatus.setBorder(new EmptyBorder(9, 13, 9, 13));
        header.add(databaseStatus, BorderLayout.EAST);

        JPanel surface = new JPanel(new BorderLayout());
        surface.setBackground(SURFACE);
        surface.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 12, 12, 12)));
        pageCards.setBackground(SURFACE);
        surface.add(pageCards, BorderLayout.CENTER);

        workspace.add(header, BorderLayout.NORTH);
        workspace.add(surface, BorderLayout.CENTER);
        return workspace;
    }

    private void addPage(String key, String title, String description, JPanel page) {
        page.setBackground(SURFACE);
        page.setName(key);
        styleTables(page);
        pageCards.add(page, key);
        page.putClientProperty("page.title", title);
        page.putClientProperty("page.description", description);
    }

    private void showPage(String key) {
        pageLayout.show(pageCards, key);
        JPanel page = null;
        for (Component component : pageCards.getComponents()) {
            if (component instanceof JPanel && key.equals(component.getName())) {
                page = (JPanel) component;
                break;
            }
        }
        if (page != null) {
            pageTitle.setText((String) page.getClientProperty("page.title"));
            pageDescription.setText((String) page.getClientProperty("page.description"));
        }
        for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
            boolean selected = entry.getKey().equals(key);
            entry.getValue().setBackground(selected ? ACCENT_DARK : NAVY);
            entry.getValue().setForeground(selected ? Color.WHITE : new Color(225, 234, 245));
            entry.getValue().setFont(APP_FONT.deriveFont(selected ? Font.BOLD : Font.PLAIN, 13f));
            entry.getValue().repaint();
        }
    }

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Tables in spms_db");
        title.setFont(APP_FONT.deriveFont(Font.BOLD, 16f));
        title.setForeground(NAVY);

        JButton refreshBtn = new JButton("Refresh");
        styleActionButton(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(SURFACE);
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refresh() {
        try {
            table.setModel(Database.tableCounts());
            databaseStatus.setText("Connected  |  spms_db");
            databaseStatus.setBackground(new Color(226, 244, 240));
            databaseStatus.setForeground(new Color(20, 112, 95));
        } catch (SQLException ex) {
            databaseStatus.setText("Unavailable  |  spms_db");
            databaseStatus.setBackground(new Color(255, 235, 232));
            databaseStatus.setForeground(new Color(170, 55, 45));
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTables(Component component) {
        if (component instanceof JTable) {
            JTable dataTable = (JTable) component;
            dataTable.setFont(APP_FONT);
            dataTable.setRowHeight(31);
            dataTable.setShowVerticalLines(false);
            dataTable.setGridColor(new Color(231, 236, 242));
            dataTable.setSelectionBackground(new Color(218, 241, 238));
            dataTable.setSelectionForeground(NAVY);
            dataTable.setFillsViewportHeight(true);
            dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JTableHeader header = dataTable.getTableHeader();
            header.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
            header.setBackground(new Color(239, 244, 248));
            header.setForeground(NAVY);
            header.setReorderingAllowed(false);
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        }
        if (component instanceof JButton) {
            styleActionButton((JButton) component);
        }
        if (component instanceof JTextField) {
            JTextField field = (JTextField) component;
            field.setFont(APP_FONT);
            field.setMargin(new Insets(7, 9, 7, 9));
        }
        if (component instanceof JTextArea) {
            JTextArea area = (JTextArea) component;
            area.setFont(APP_FONT);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
        }
        if (component instanceof JComboBox) {
            ((JComboBox<?>) component).setFont(APP_FONT);
        }
        if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            list.setFont(APP_FONT);
            list.setSelectionBackground(new Color(218, 241, 238));
            list.setSelectionForeground(NAVY);
        }
        if (component instanceof JTree) {
            JTree tree = (JTree) component;
            tree.setFont(APP_FONT);
            tree.setRowHeight(25);
        }
        if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(231, 236, 242)));
        }
        if (component instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) component;
            splitPane.setBorder(null);
            splitPane.setDividerSize(8);
            splitPane.setContinuousLayout(true);
        }
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            if (panel.getBorder() instanceof TitledBorder) {
                TitledBorder border = (TitledBorder) panel.getBorder();
                border.setTitleFont(APP_FONT.deriveFont(Font.BOLD, 13f));
                border.setTitleColor(NAVY);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                styleTables(child);
            }
        }
    }

    private void styleActionButton(JButton button) {
        String label = button.getText() == null ? "" : button.getText().toLowerCase();
        Color background = ACCENT;
        Color foreground = Color.WHITE;
        if (label.contains("delete") || label.contains("reject")) {
            background = new Color(255, 238, 236);
            foreground = new Color(157, 48, 43);
        } else if (label.contains("refresh") || label.contains("edit") || label.contains("rename")) {
            background = new Color(237, 243, 248);
            foreground = NAVY;
        } else if (label.contains("approve") || label.contains("save") || label.contains("generate")
                || label.contains("add") || label.contains("new") || label.contains("link")) {
            background = ACCENT;
        }
        button.setFont(APP_FONT.deriveFont(Font.BOLD, 12f));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setToolTipText(button.getText());
    }

    public static void main(String[] args) {
        installTheme();
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private static void installTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("Label.font", APP_FONT);
        UIManager.put("Button.font", APP_FONT);
        UIManager.put("TextField.font", APP_FONT);
        UIManager.put("TextArea.font", APP_FONT);
        UIManager.put("ComboBox.font", APP_FONT);
        UIManager.put("Table.font", APP_FONT);
        UIManager.put("TableHeader.font", APP_FONT.deriveFont(Font.BOLD));
        UIManager.put("OptionPane.messageFont", APP_FONT);
        UIManager.put("OptionPane.buttonFont", APP_FONT);
    }

    private static class NavigationButton extends JButton {
        NavigationButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}