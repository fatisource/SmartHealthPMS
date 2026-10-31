import java.sql.*;
public class ProbeTables {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "")) {
      DatabaseMetaData md = c.getMetaData();
      try (ResultSet rs = md.getTables(null, null, null, new String[]{"TABLE"})) {
        while (rs.next()) {
          String t = rs.getString("TABLE_NAME");
          System.out.println("TABLE " + t);
          try (ResultSet cols = md.getColumns(null, null, t, null)) {
            while (cols.next()) {
              System.out.println("  " + cols.getString("COLUMN_NAME") + " " + cols.getString("TYPE_NAME"));
            }
          }
          System.out.println();
        }
      }
    }
  }
}
