package spms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class EvmCalculator {
    private static final int SCALE = 4;

    private EvmCalculator() {}

    public static EvmReport calculate(Connection c, int projectId) throws SQLException {
        BigDecimal projectBudget = BigDecimal.ZERO;
        try (PreparedStatement ps = c.prepareStatement("SELECT approved_budget FROM project WHERE id = ?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectBudget = rs.getBigDecimal("approved_budget");
                }
            }
        }

        if (projectBudget == null) {
            projectBudget = BigDecimal.ZERO;
        }

        BigDecimal pv = BigDecimal.ZERO;
        BigDecimal ev = BigDecimal.ZERO;
        BigDecimal ac = BigDecimal.ZERO;
        List<CostActivityRow> rows = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, code, name, planned_cost, actual_cost, percent_complete, status FROM activity WHERE project_id = ? ORDER BY id")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String code = rs.getString("code");
                    String name = rs.getString("name");
                    BigDecimal plannedCost = rs.getBigDecimal("planned_cost");
                    BigDecimal actualCost = rs.getBigDecimal("actual_cost");
                    int percent = rs.getInt("percent_complete");
                    String status = rs.getString("status");

                    BigDecimal scheduledValue = plannedCost.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP));
                    BigDecimal earnedValue = scheduledValue;
                    BigDecimal actualValue = actualCost;

                    pv = pv.add(plannedCost);
                    ev = ev.add(earnedValue);
                    ac = ac.add(actualValue);

                    CostActivityRow row = new CostActivityRow();
                    row.id = id;
                    row.code = code;
                    row.name = name;
                    row.plannedCost = plannedCost;
                    row.actualCost = actualCost;
                    row.percentComplete = percent;
                    row.status = status;
                    row.pv = scheduledValue;
                    row.ev = earnedValue;
                    row.ac = actualValue;
                    row.cv = earnedValue.subtract(actualValue);
                    row.sv = earnedValue.subtract(scheduledValue);
                    rows.add(row);
                }
            }
        }

        BigDecimal cv = ev.subtract(ac);
        BigDecimal sv = ev.subtract(pv);
        BigDecimal cpi = ac.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : ev.divide(ac, SCALE, RoundingMode.HALF_UP);
        BigDecimal spi = pv.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : ev.divide(pv, SCALE, RoundingMode.HALF_UP);
        BigDecimal cpiPercent = cpi.multiply(BigDecimal.valueOf(100));
        BigDecimal spiPercent = spi.multiply(BigDecimal.valueOf(100));
        BigDecimal vac = projectBudget.subtract(ac);
        BigDecimal eac = ac.compareTo(BigDecimal.ZERO) == 0 ? projectBudget : projectBudget.divide(cpi, SCALE, RoundingMode.HALF_UP);
        BigDecimal etc = eac.subtract(ac);
        BigDecimal ctc = projectBudget.subtract(ev);

        EvmReport report = new EvmReport();
        report.budgetAtCompletion = projectBudget;
        report.plannedValue = pv;
        report.earnedValue = ev;
        report.actualCost = ac;
        report.costVariance = cv;
        report.scheduleVariance = sv;
        report.costPerformanceIndex = cpi;
        report.schedulePerformanceIndex = spi;
        report.varianceAtCompletion = vac;
        report.estimateAtCompletion = eac;
        report.estimateToComplete = etc;
        report.toCompleteCostPerformanceIndex = cpi.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : projectBudget.subtract(ev).divide(ac, SCALE, RoundingMode.HALF_UP);
        report.costPerformanceIndexPercent = cpiPercent;
        report.schedulePerformanceIndexPercent = spiPercent;
        report.costToComplete = ctc;
        report.rows = rows;
        return report;
    }

    public static final class EvmReport {
        public BigDecimal budgetAtCompletion;
        public BigDecimal plannedValue;
        public BigDecimal earnedValue;
        public BigDecimal actualCost;
        public BigDecimal costVariance;
        public BigDecimal scheduleVariance;
        public BigDecimal costPerformanceIndex;
        public BigDecimal schedulePerformanceIndex;
        public BigDecimal varianceAtCompletion;
        public BigDecimal estimateAtCompletion;
        public BigDecimal estimateToComplete;
        public BigDecimal toCompleteCostPerformanceIndex;
        public BigDecimal costPerformanceIndexPercent;
        public BigDecimal schedulePerformanceIndexPercent;
        public BigDecimal costToComplete;
        public List<CostActivityRow> rows;
    }

    public static final class CostActivityRow {
        public int id;
        public String code;
        public String name;
        public BigDecimal plannedCost;
        public BigDecimal actualCost;
        public int percentComplete;
        public String status;
        public BigDecimal pv;
        public BigDecimal ev;
        public BigDecimal ac;
        public BigDecimal cv;
        public BigDecimal sv;
    }
}
