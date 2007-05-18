public class Static {

  static final int x;
  static final int y;

  static {
	x = 42;
	y = 69;
  }

  public void testStaticBlock()
  {
    int x = 22;

    {
      int y = 33;
      x += y;
    }

    System.out.println(x);
  }
}
