import java.sql.*;
public class ProbeSchema {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "")) {
      String[] tables = {"team_member","wbs_item","project","project_plan","requirement","activity","change_request","milestone"};
      for (String t : tables) {
        System.out.println("TABLE " + t);
        try (PreparedStatement ps = c.prepareStatement("DESCRIBE `" + t + "`")) {
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              System.out.println(rs.getString(1) + " | " + rs.getString(2));
            }
          }
        }
        System.out.println();
      }
    }
  }
}
