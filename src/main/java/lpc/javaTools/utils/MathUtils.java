package lpc.javaTools.utils;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.function.ToDoubleFunction;

public class MathUtils {
	public static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}
	public static double unlerp(double a, double b, double t) {
		return (t - a) / (b - a);
	}
	public static double min(double... vals) {
		double min = vals[0];
		for (int i = 1; i < vals.length; i++)
			if (vals[i] < min)
				min = vals[i];
		return min;
	}
	public static double sum(double... vals) {
		double sum = 0;
		for (double val : vals)
			sum += val;
		return sum;
	}
	public static double average(double... vals) {
		return sum(vals) / vals.length;
	}
	public static double[] apply(Double2DoubleFunction function, double... vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++)
			result[i] = function.apply(vals[i]);
		return result;
	}
	public static void selfApply(Double2DoubleFunction function, double... vals) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.apply(vals[i]);
	}
	public static void selfApply(Int2IntFunction function, int... vals) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.apply(vals[i]);
	}
	public static double[] apply(Int2DoubleFunction function, int... vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++)
			result[i] = function.apply(vals[i]);
		return result;
	}
	@SafeVarargs public static <T> double[] apply(ToDoubleFunction<T> function, T... vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++)
			result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
}
