import java.lang.Math;

class MathTest
{
	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static int max(int a, int b) {
		return Math.max(a, b);
	}

	public static Double toDegrees(Double radians) {
		return radians * 180 / Math.PI;
	}
}
