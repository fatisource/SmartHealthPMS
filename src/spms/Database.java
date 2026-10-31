package spms;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Shared MySQL connection (XAMPP) for the whole application. */
public final class Database {

    private static final String URL =
        "jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";   // XAMPP default is empty

    private static Connection conn;

    private Database() {}

    public static synchronized Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }

    /** Returns a read-only table model listing every table and its row count. */
    public static DefaultTableModel tableCounts() throws SQLException {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Table", "Rows"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<String> tables = new ArrayList<>();
        try (Statement st = get().createStatement();
             ResultSet rs = st.executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }

        for (String t : tables) {
            try (Statement st = get().createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + t + "`")) {
                rs.next();
                model.addRow(new Object[]{t, rs.getInt(1)});
            }
        }
        return model;
    }
}