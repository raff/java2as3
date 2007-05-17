package tested;

class Test2 extends Test
{
  public Test2(int one, String two, Object three) 
  {
  }

  public int intReturn(String a, Object b) 
  {
    return 42; 
  }

  public int testReserved(int is, int in)
  {
	is = is + in;
        in = in + is;
	return is * in;
  }

  public int testGetSet(int get, int set)
  {
    return get + 22 - set;
  }

}
