package lpc.javaTools.utils;

public class MathUtils {
	public static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}
	public static double unlerp(double a, double b, double t) {
		return (t - a) / (b - a);
	}
}
