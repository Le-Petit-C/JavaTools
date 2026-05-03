package lpc.javaTools.utils.math;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lpc.javaTools.utils.math.interfaces.ToFloatFunction;
import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MathUtils {
	public static float lengthSquared(float x, float y) { return x * x + y * y; }
	public static double square(double x) { return x * x; }
	public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
	public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
	public static double unlerp(double a, double b, double t) { return (t - a) / (b - a); }
	public static float unlerp(float a, float b, float t) { return (t - a) / (b - a); }
	
	// 在0~1的循环区间上进行线性插值，找最近的方向插值
	// 假设a和b都在0~1区间上
	public static float roundLerp(float a, float b, float t) {
		if(Math.abs(a - b) > 0.5f) {
			if(a < b) ++a;
			else ++b;
		}
		float result = lerp(a, b, t);
		if(result >= 1) --result;
		return result;
	}
	
	public static double min(double[] vals) {
		double min = vals[0];
		for (int i = 1; i < vals.length; i++)
			if (vals[i] < min)
				min = vals[i];
		return min;
	}
	public static float min(float[] vals) {
		float min = vals[0];
		for (int i = 1; i < vals.length; i++)
			if (vals[i] < min)
				min = vals[i];
		return min;
	}
	public static double sum(double[] vals) {
		double sum = 0;
		for (double val : vals)
			sum += val;
		return sum;
	}
	public static double sum(Double2DoubleFunction function, double[] vals) {
		double sum = 0;
		for (double val : vals)
			sum += function.applyAsDouble(val);
		return sum;
	}
	public static double sum(Int2DoubleFunction function, int[] vals) {
		double sum = 0;
		for (int val : vals)
			sum += function.applyAsDouble(val);
		return sum;
	}
	public static double average(double[] vals) {
		return sum(vals) / vals.length;
	}
	public static double average(Double2DoubleFunction function, double[] vals) {
		return sum(function, vals) / vals.length;
	}
	public static double average(Int2DoubleFunction function, int[] vals) {
		return sum(function, vals) / vals.length;
	}
	public static double[] apply(Double2DoubleFunction function, double[] vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++)
			result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
	public static double[] selfApply(Double2DoubleFunction function, double[] vals) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.applyAsDouble(vals[i]);
		return vals;
	}
	public static float[] selfApply(Float2FloatFunction function, float[] vals) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.get(vals[i]);
		return vals;
	}
	public static int[] selfApply(Int2IntFunction function, int[] vals) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.applyAsInt(vals[i]);
		return vals;
	}
	public static double[] selfInverse(double[] arr) {
		int i = 0, j = arr.length - 1;
		while (i < j) {
			double tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
			++i;
			--j;
		}
		return arr;
	}
	public static float[] selfInverse(float[] arr) {
		int i = 0, j = arr.length - 1;
		while (i < j) {
			float tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
			++i;
			--j;
		}
		return arr;
	}
	public static double[] apply(Int2DoubleFunction function, int[] vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
	public static <T> double[] apply(ToDoubleFunction<T> function, T[] vals) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
	public static <T> float[] toFloatApply(ToFloatFunction<T> function, T[] vals) {
		float[] result = new float[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsFloat(vals[i]);
		return result;
	}
	public static <T> float[] apply(ToFloatFunction<T> function, T[] vals) {
		return toFloatApply(function, vals);
	}
	public static double[] convolution(double[] a, double[] b) {
		double[] result = new double[a.length + b.length - 1];
		for(int i = 0; i < a.length; ++i)
			for(int j = 0; j < b.length; ++j)
				result[i + j] += a[i] * b[j];
		return result;
	}
	
	public static double[] fastConvolution(double[] a, double[] b) {
		int resultSize = a.length + b.length - 1;
		
		// 取 >= resultSize 的 2 的幂（FFT更快）
		int n = 1;
		while (n < resultSize) n <<= 1;
		
		// 复数数组（长度 = 2*n）
		double[] fa = new double[2 * n];
		double[] fb = new double[2 * n];
		
		// 拷贝数据到实部（虚部默认0）
		for (int i = 0; i < a.length; i++) {
			fa[2 * i] = a[i];
		}
		for (int i = 0; i < b.length; i++) {
			fb[2 * i] = b[i];
		}
		
		DoubleFFT_1D fft = new DoubleFFT_1D(n);
		
		// 正变换
		fft.complexForward(fa);
		fft.complexForward(fb);
		
		// 频域复数乘法
		for (int i = 0; i < n; i++) {
			int re = 2 * i;
			int im = re + 1;
			
			double ar = fa[re];
			double ai = fa[im];
			double br = fb[re];
			double bi = fb[im];
			
			// (ar + ai*i) * (br + bi*i)
			fa[re] = ar * br - ai * bi;
			fa[im] = ar * bi + ai * br;
		}
		
		// 逆变换（true = 自动除以 n）
		fft.complexInverse(fa, true);
		
		// 取实部作为结果
		double[] result = new double[resultSize];
		for (int i = 0; i < resultSize; i++)
			result[i] = fa[2 * i];
		
		return result;
	}
	
	public static float[] fastConvolution(float[] a, float[] b) {
		int resultSize = a.length + b.length - 1;
		
		// 取 >= resultSize 的 2 的幂（FFT更快）
		int n = 1;
		while (n < resultSize) n <<= 1;
		
		// 复数数组（长度 = 2*n）
		float[] fa = new float[2 * n];
		float[] fb = new float[2 * n];
		
		// 拷贝数据到实部（虚部默认0）
		for (int i = 0; i < a.length; i++) {
			fa[2 * i] = a[i];
		}
		for (int i = 0; i < b.length; i++) {
			fb[2 * i] = b[i];
		}
		
		FloatFFT_1D fft = new FloatFFT_1D(n);
		
		// 正变换
		fft.complexForward(fa);
		fft.complexForward(fb);
		
		// 频域复数乘法
		for (int i = 0; i < n; i++) {
			int re = 2 * i;
			int im = re + 1;
			
			float ar = fa[re];
			float ai = fa[im];
			float br = fb[re];
			float bi = fb[im];
			
			// (ar + ai*i) * (br + bi*i)
			fa[re] = ar * br - ai * bi;
			fa[im] = ar * bi + ai * br;
		}
		
		// 逆变换（true = 自动除以 n）
		fft.complexInverse(fa, true);
		
		// 取实部作为结果
		float[] result = new float[resultSize];
		for (int i = 0; i < resultSize; i++)
			result[i] = fa[2 * i];
		
		return result;
	}
	
	public static boolean isRectArray(Object[][] array) {
		if(array == null) return false;
		if(array.length == 0) return true;
		int l = array[0].length;
		for(var arr : array)
			if(arr.length != l)
				return false;
		return true;
	}
	
	public static boolean isRectArray(int[][] array) {
		if(array == null) return false;
		if(array.length == 0) return true;
		int l = array[0].length;
		for(var arr : array)
			if(arr.length != l)
				return false;
		return true;
	}
	
	public static int maxIndexOf(double[] arr) {
		if(arr == null || arr.length == 0) throw new IllegalArgumentException("Array is null or empty");
		double max = arr[0];
		int index = 0;
		for(int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}
		return index;
	}
	
	public static int maxIndexOf(float[] arr) {
		if(arr == null || arr.length == 0) throw new IllegalArgumentException("Array is null or empty");
		float max = arr[0];
		int index = 0;
		for(int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
				index = i;
			}
		}
		return index;
	}
	
	public static double max(double[] values, Double2DoubleFunction function) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (double value : values) maxValue = Math.max(maxValue, function.applyAsDouble(value));
		return maxValue;
	}
	
	public static double max(double[] values) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (double value : values) maxValue = Math.max(maxValue, value);
		return maxValue;
	}
	
	public static float floatMax(float[] values) {
		float maxValue = Float.NEGATIVE_INFINITY;
		for (float value : values) maxValue = Math.max(maxValue, value);
		return maxValue;
	}
	
	public static float max(float[] values) { return floatMax(values); }
	
	public static <T> double max(Iterable<T> values, ToDoubleFunction<T> function) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsDouble(value));
		return maxValue;
	}
	
	public static <T> double doubleMax(Iterable<T> values, ToDoubleFunction<T> function) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsDouble(value));
		return maxValue;
	}
	
	public static <T> int max(Iterable<T> values, ToIntFunction<T> function) {
		int maxValue = Integer.MIN_VALUE;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsInt(value));
		return maxValue;
	}
	
	public static <T> int intMax(Iterable<T> values, ToIntFunction<T> function) {
		int maxValue = Integer.MIN_VALUE;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsInt(value));
		return maxValue;
	}
}
