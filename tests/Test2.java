package tests;

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

  public void testForeach(java.util.AbstractMap<String, String> m)
  {
	for (String k : m.keySet())
		System.out.println(k);
  }

  public void testEach(java.util.AbstractMap<String, String> m)
  {
	for (String each : m.keySet())
		System.out.println(each);
  }
}
