import java.io.File;
import java.util.ArrayList;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class CompileProject {
  public static void main(String[] args) throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) throw new RuntimeException("No compiler");
    File outDir = new File("E:\\SmartHealthPMS\\out");
    if (!outDir.exists()) outDir.mkdirs();
    ArrayList<String> cmd = new ArrayList<>();
    cmd.add("-encoding"); cmd.add("UTF-8");
    cmd.add("-cp"); cmd.add("E:\\SmartHealthPMS\\lib\\mysql-connector-j-9.1.0.jar");
    cmd.add("-d"); cmd.add(outDir.getAbsolutePath());
    File spmsDir = new File("E:\\SmartHealthPMS\\src\\spms");
    for (File f : spmsDir.listFiles((d, n) -> n.endsWith(".java"))) {
      cmd.add(f.getAbsolutePath());
    }
    int rc = compiler.run(null, null, null, cmd.toArray(new String[0]));
    System.out.println("compile_exit=" + rc);
  }
}
