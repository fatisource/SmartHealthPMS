import java.sql.*;
public class SchemaProbe {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "");
         Statement st = c.createStatement()) {
      ResultSet tables = st.executeQuery("SHOW TABLES");
      while (tables.next()) {
        String t = tables.getString(1);
        System.out.println("TABLE " + t);
        try (Statement st2 = c.createStatement();
             ResultSet cols = st2.executeQuery("SHOW COLUMNS FROM `" + t + "`")) {
          while (cols.next()) {
            System.out.println("  " + cols.getString("Field") + " " + cols.getString("Type") + " " + cols.getString("Null") + " " + cols.getString("Key") + " " + cols.getString("Default"));
          }
        }
      }
    }
  }
}
