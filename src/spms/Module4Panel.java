package spms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Module4Panel extends JPanel {
    private static final int PROJECT_ID = 1;

    private final JLabel bacLabel = new JLabel("BAC: -");
    private final JLabel pvLabel = new JLabel("PV: -");
    private final JLabel evLabel = new JLabel("EV: -");
    private final JLabel acLabel = new JLabel("AC: -");
    private final JLabel cvLabel = new JLabel("CV: -");
    private final JLabel svLabel = new JLabel("SV: -");
    private final JLabel cpiLabel = new JLabel("CPI: -");
    private final JLabel spiLabel = new JLabel("SPI: -");

    private final DefaultTableModel metricModel = new DefaultTableModel(
            new Object[]{"Metric", "Value", "Interpretation"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable metricTable = new JTable(metricModel);

    private final DefaultTableModel activityCostModel = new DefaultTableModel(
            new Object[]{"ID", "Code", "Name", "Planned", "Actual", "EV", "CV", "%", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable activityCostTable = new JTable(activityCostModel);

    public Module4Panel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(2, 4, 10, 10));
        top.add(bacLabel);
        top.add(pvLabel);
        top.add(evLabel);
        top.add(acLabel);
        top.add(cvLabel);
        top.add(svLabel);
        top.add(cpiLabel);
        top.add(spiLabel);

        JPanel metricPanel = new JPanel(new BorderLayout(8, 8));
        metricPanel.setBorder(BorderFactory.createTitledBorder("EVM Summary"));
        metricPanel.add(new JScrollPane(metricTable), BorderLayout.CENTER);

        JPanel activityPanel = new JPanel(new BorderLayout(8, 8));
        activityPanel.setBorder(BorderFactory.createTitledBorder("Activity Cost / EVM"));
        activityPanel.add(new JScrollPane(activityCostTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        buttonPanel.add(refreshBtn);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(top, BorderLayout.NORTH);
        center.add(metricPanel, BorderLayout.CENTER);
        center.add(activityPanel, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        try (Connection c = Database.get()) {
            EvmCalculator.EvmReport report = EvmCalculator.calculate(c, PROJECT_ID);
            populateMetricSummary(report);
            populateActivityTable(report);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to calculate EVM data. " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateMetricSummary(EvmCalculator.EvmReport report) {
        bacLabel.setText("BAC: PKR " + fmt(report.budgetAtCompletion));
        pvLabel.setText("PV: PKR " + fmt(report.plannedValue));
        evLabel.setText("EV: PKR " + fmt(report.earnedValue));
        acLabel.setText("AC: PKR " + fmt(report.actualCost));
        cvLabel.setText("CV: PKR " + fmt(report.costVariance));
        svLabel.setText("SV: PKR " + fmt(report.scheduleVariance));
        cpiLabel.setText("CPI: " + fmt(report.costPerformanceIndex));
        spiLabel.setText("SPI: " + fmt(report.schedulePerformanceIndex));

        metricModel.setRowCount(0);
        addMetricRow("Budget at Completion (BAC)", fmt(report.budgetAtCompletion), "Approved budget baseline");
        addMetricRow("Planned Value (PV)", fmt(report.plannedValue), "Budgeted work scheduled");
        addMetricRow("Earned Value (EV)", fmt(report.earnedValue), "Budgeted work actually completed");
        addMetricRow("Actual Cost (AC)", fmt(report.actualCost), "Real cost incurred");
        addMetricRow("Cost Variance (CV)", fmt(report.costVariance), describeVariance(report.costVariance, "Cost"));
        addMetricRow("Schedule Variance (SV)", fmt(report.scheduleVariance), describeVariance(report.scheduleVariance, "Schedule"));
        addMetricRow("Cost Performance Index (CPI)", fmt(report.costPerformanceIndex), describeIndex(report.costPerformanceIndex, "CPI"));
        addMetricRow("Schedule Performance Index (SPI)", fmt(report.schedulePerformanceIndex), describeIndex(report.schedulePerformanceIndex, "SPI"));
        addMetricRow("Estimate at Completion (EAC)", fmt(report.estimateAtCompletion), "Projected total cost if current trend continues");
        addMetricRow("Estimate to Complete (ETC)", fmt(report.estimateToComplete), "Expected additional cost to finish");
        addMetricRow("Variance at Completion (VAC)", fmt(report.varianceAtCompletion), "Potential over/under budget at finish");
    }

    private void populateActivityTable(EvmCalculator.EvmReport report) {
        activityCostModel.setRowCount(0);
        for (EvmCalculator.CostActivityRow row : report.rows) {
            activityCostModel.addRow(new Object[]{
                    row.id,
                    row.code,
                    row.name,
                    fmt(row.plannedCost),
                    fmt(row.actualCost),
                    fmt(row.ev),
                    fmt(row.cv),
                    row.percentComplete + "%",
                    row.status
            });
        }
    }

    private void addMetricRow(String metric, String value, String interpretation) {
        metricModel.addRow(new Object[]{metric, value, interpretation});
    }

    private String describeVariance(BigDecimal value, String label) {
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            return label + " is favorable (ahead/under budget).";
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return label + " is unfavorable (behind/over budget).";
        }
        return label + " is on target.";
    }

    private String describeIndex(BigDecimal value, String label) {
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return label + " is above target (good).";
        }
        if (value.compareTo(BigDecimal.ONE) < 0) {
            return label + " is below target (needs attention).";
        }
        return label + " is exactly on target.";
    }

    private String fmt(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
