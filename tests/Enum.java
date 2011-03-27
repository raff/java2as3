public enum Enum {

	E_ONE(1),
	E_TWO(2),
	E_THREE(3),
	E_FOUR(4);

	private int _value;
	Enum(int v) { _value = v; };
	public int value() { return _value; }
}
