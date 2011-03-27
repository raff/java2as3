import java.io.*;

class Xception extends Exception implements Serializable
{
  static final long serialVersionUID = 1;

  Xception(String x)
  {
  }

  public static void main(String args[]) throws Exception
  {
    throw new Xception("hello xception!");
  }
}
