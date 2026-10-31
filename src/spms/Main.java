package spms;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private final JTable table = new JTable();
    private final JLabel status = new JLabel(" ");

    public Main() {
        super("Smart Healthcare Project Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Setup Check", buildSetupPanel());
        tabs.addTab("1. Initiation", new Module1Panel());
        tabs.addTab("2. Scope & WBS", new Module2Panel());
        tabs.addTab("3. Schedule", new Module3Panel());
        tabs.addTab("4. Cost & EVM", new Module4Panel());
        tabs.addTab("5. Quality & Risk", new Module5Panel());
        tabs.addTab("6. Dashboard", new Module6Panel());
        add(tabs);

        refresh();
    }

    private JPanel buildSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Database tables and row counts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(status, BorderLayout.SOUTH);
        return panel;
    }

    private void refresh() {
        try {
            table.setModel(Database.tableCounts());
            status.setText("Connected to spms_db (" + table.getRowCount() + " tables)");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Connection failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}