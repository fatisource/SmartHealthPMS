import java.sql.*;
public class Probe5 {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "")) {
      String[] tables = {"defect", "quality_check", "risk"};
      for (String t : tables) {
        System.out.println("TABLE " + t);
        String q = "SELECT * FROM `" + t + "` WHERE project_id = 1 ORDER BY id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(q)) {
          ResultSetMetaData md = rs.getMetaData();
          while (rs.next()) {
            StringBuilder sb = new StringBuilder();
            for (int i=1; i<=md.getColumnCount(); i++) {
              sb.append(md.getColumnName(i)).append("=").append(rs.getObject(i)).append(" | ");
            }
            System.out.println(sb);
          }
        }
        System.out.println();
      }
    }
  }
}
