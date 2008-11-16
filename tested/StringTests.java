class StringTests
{
	static public boolean compare(String s1, String s2)
	{
		return s1.equals(s2);
	}

	static public boolean compareIgnoreCase(String s1, String s2)
	{
		return s1.equalsIgnoreCase(s2);
	}

	static void append(StringBuilder sb, String s)
	{
		sb.append(s);
	}
}
