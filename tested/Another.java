package test;

public abstract class Another extends java.util.HashMap
{
	public void something()
	{
		Object x = new Object() {
			public String toString() { return "aString"; }
		};
	}
}
