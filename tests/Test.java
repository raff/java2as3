package tests;

import java.util.Map;
import java.util.HashMap;

public class Test
{
	private final static String something;
	public double number;
	String aString = "aString";
	
	static {
		something = "42";
	}

  // Constructors
  public Test() {}
  public Test(int one, String two, Object three) {}
  //private Test(Object... p) {}

  public void noReturn(int a, int b, int c) {}

  public int intReturn(String a, Object b) { return 0; }

  public String stringReturn(Object o)
  {
	int one, two, three;
	String s = "fortytwo";
	long l, l1=1, l2=2, l3 = ~1L;

	return o.toString();
  }

  public void exec(String x) throws Exception
  {
	java.util.Map<String, String> m = new java.util.HashMap<String, String>();
	if (m.put(x, x) == null)
		throw new Exception("blah blah blah");

  }

  final public void params(String... p) {
	// do something
	try
	{
		if (p.length < 10)
		throw new RuntimeException("something bad");
	} catch(Exception e) {
		throw new RuntimeException(e);
	} finally {
		System.out.println("goodbye");
	}
  }

  private char nullchar = '0';

  private Integer ii = new Integer(23);
  private Float ff = new Float(3.14);
  private Double dd = new Double(ff);

  private double another(Float f)
  {
    int x[][] = new int[22][22];
    int y[] = { 1, 2, 3};
    int z[] = { 0 };
    return (double) ((Float) ((double) f));
  }

  public long currentTime() { return System.currentTimeMillis(); }

  public boolean isInteger(Object x)
  {
    boolean n = x instanceof Short;
    return (x instanceof Integer) || n;
  }
}
