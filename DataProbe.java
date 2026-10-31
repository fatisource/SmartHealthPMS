import java.sql.*;
public class DataProbe {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/spms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "")) {
      String[][] queries = {
        {"project", "SELECT * FROM project WHERE id = 1"},
        {"project_plan", "SELECT * FROM project_plan WHERE project_id = 1"},
        {"stakeholder", "SELECT * FROM stakeholder WHERE project_id = 1 ORDER BY id"},
        {"team_member", "SELECT * FROM team_member WHERE project_id = 1 ORDER BY id"},
        {"milestone", "SELECT * FROM milestone WHERE project_id = 1 ORDER BY id"}
      };
      for (String[] q : queries) {
        System.out.println("TABLE " + q[0]);
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(q[1])) {
          ResultSetMetaData md = rs.getMetaData();
          while (rs.next()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= md.getColumnCount(); i++) {
              sb.append(md.getColumnName(i)).append("=").append(rs.getObject(i)).append(" | ");
            }
            System.out.println(sb);
          }
        }
      }
    }
  }
}
