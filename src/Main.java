
import spoon.Launcher;

public class Main
{
  private static String launcherArgs[] = {
	"--no", "-p", "Java2AS3Processor"
  };

  public static void main(String args[])
	throws Exception
  {
	String params[] = new String[launcherArgs.length + args.length];
	System.arraycopy(launcherArgs, 0, params, 0, launcherArgs.length);
	System.arraycopy(args, 0, params, launcherArgs.length, args.length);
	Launcher.main(params);
  }
}
