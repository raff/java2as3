package test;

public abstract class Another extends java.util.HashMap
{
	public abstract void abstractMethod();

	public void something()
	{
/*
		Object x = new Object() {
			public String toString() { return "aString"; }
		};
*/
	}

	public void something(String elze)
	{
	}

	public void hello()
	{
		something();
		something("else");
	}
}
