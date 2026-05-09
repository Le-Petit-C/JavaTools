package lpc.javaTools.utils.math;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.DoubleIterable;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.floats.FloatIterable;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import lpc.javaTools.utils.math.interfaces.ToFloatFunction;
import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.fft.FloatFFT_1D;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MathUtils {
	public static float norm2Squared(float x, float y) { return x * x + y * y; }
	
	public static double square(double x) { return x * x; }
	public static float square(float x) { return x * x; }
	public static int square(int x) { return x * x; }
	
	public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
	public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
	public static double unlerp(double a, double b, double t) { return (t - a) / (b - a); }
	public static float unlerp(float a, float b, float t) { return (t - a) / (b - a); }
	
	// 二次插值，认定y1,y2,y3分别是x=-1,0,1时的函数取值，做二次插值
	public static double qerp(double y1, double y2, double y3, double x) {
		double a = (y3 + y1) * 0.5 - y2;
		double b = (y3 - y1) * 0.5;
		return (a * x + b) * x + y2;
	}
	public static float qerp(float y1, float y2, float y3, float x) {
		float a = (y3 + y1) * 0.5f - y2;
		float b = (y3 - y1) * 0.5f;
		return (a * x + b) * x + y2;
	}
	
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
	
	public interface DoubleAccumulator { double accumulateDouble(double a, double previousAccumulated); }
	public interface FloatAccumulator { float accumulateFloat(float a, float previousAccumulated); }
	public interface IntAccumulator { int accumulateInt(int a, int previousAccumulated); }
	public interface DoublePairAccumulator { double accumulateDoublePair(double a, double b, double previousAccumulated); }
	public interface FloatPairAccumulator { float accumulateFloatPair(float a, float b, float previousAccumulated); }
	public interface IntPairAccumulator { int accumulateIntPair(int a, int b, int previousAccumulated); }
	public static double accumulateDoubles(double[] vals, DoubleAccumulator accumulator) {
		double res = 0.0;
		for(double val : vals) res = accumulator.accumulateDouble(val, res);
		return res;
	}
	public static double accumulateDoubles(DoubleIterable vals, DoubleAccumulator accumulator) {
		double res = 0.0;
		DoubleIterator it = vals.iterator();
		while(it.hasNext()) res = accumulator.accumulateDouble(it.nextDouble(), res);
		return res;
	}
	public static float accumulateFloats(float[] vals, FloatAccumulator accumulator) {
		float res = 0.0f;
		for(float val : vals) res = accumulator.accumulateFloat(val, res);
		return res;
	}
	public static float accumulateFloats(FloatIterable vals, FloatAccumulator accumulator) {
		float res = 0.0f;
		FloatIterator it = vals.iterator();
		while(it.hasNext()) res = accumulator.accumulateFloat(it.nextFloat(), res);
		return res;
	}
	public static int accumulateInts(int[] vals, IntAccumulator accumulator) {
		int res = 0;
		for(int val : vals) res = accumulator.accumulateInt(val, res);
		return res;
	}
	public static int accumulateInts(IntIterable vals, IntAccumulator accumulator) {
		int res = 0;
		IntIterator it = vals.iterator();
		while(it.hasNext()) res = accumulator.accumulateInt(it.nextInt(), res);
		return res;
	}
	public static double accumulateDoublePairs(double[] a, double[] b, DoublePairAccumulator accumulator) {
		double res = 0.0;
		int i = 0, min = Math.min(a.length, b.length);
		for(; i < min; ++i) res = accumulator.accumulateDoublePair(a[i], b[i], res);
		for(; i < a.length; ++i) res = accumulator.accumulateDoublePair(a[i], 0, res);
		for(; i < b.length; ++i) res = accumulator.accumulateDoublePair(0, b[i], res);
		return res;
	}
	public static double accumulateDoublePairs(DoubleIterable a, DoubleIterable b, DoublePairAccumulator accumulator) {
		double res = 0.0;
		DoubleIterator ita = a.iterator(), itb = b.iterator();
		while(ita.hasNext() && itb.hasNext()) res = accumulator.accumulateDoublePair(ita.nextDouble(), itb.nextDouble(), res);
		while(ita.hasNext()) res = accumulator.accumulateDoublePair(ita.nextDouble(), 0, res);
		while(itb.hasNext()) res = accumulator.accumulateDoublePair(0, itb.nextDouble(), res);
		return res;
	}
	public static float accumulateFloatPairs(float[] a, float[] b, FloatPairAccumulator accumulator) {
		float res = 0.0f;
		int i = 0, min = Math.min(a.length, b.length);
		for(; i < min; ++i) res = accumulator.accumulateFloatPair(a[i], b[i], res);
		for(; i < a.length; ++i) res = accumulator.accumulateFloatPair(a[i], 0, res);
		for(; i < b.length; ++i) res = accumulator.accumulateFloatPair(0, b[i], res);
		return res;
	}
	public static float accumulateFloatPairs(FloatIterable a, FloatIterable b, FloatPairAccumulator accumulator) {
		float res = 0.0f;
		FloatIterator ita = a.iterator(), itb = b.iterator();
		while(ita.hasNext() && itb.hasNext()) res = accumulator.accumulateFloatPair(ita.nextFloat(), itb.nextFloat(), res);
		while(ita.hasNext()) res = accumulator.accumulateFloatPair(ita.nextFloat(), 0, res);
		while(itb.hasNext()) res = accumulator.accumulateFloatPair(0, itb.nextFloat(), res);
		return res;
	}
	public static int accumulateIntPairs(int[] a, int[] b, IntPairAccumulator accumulator) {
		int res = 0;
		int i = 0, min = Math.min(a.length, b.length);
		for(; i < min; ++i) res = accumulator.accumulateIntPair(a[i], b[i], res);
		for(; i < a.length; ++i) res = accumulator.accumulateIntPair(a[i], 0, res);
		for(; i < b.length; ++i) res = accumulator.accumulateIntPair(0, b[i], res);
		return res;
	}
	public static int accumulateIntPairs(IntIterable a, IntIterable b, IntPairAccumulator accumulator) {
		int res = 0;
		IntIterator ita = a.iterator(), itb = b.iterator();
		while(ita.hasNext() && itb.hasNext()) res = accumulator.accumulateIntPair(ita.nextInt(), itb.nextInt(), res);
		while(ita.hasNext()) res = accumulator.accumulateIntPair(ita.nextInt(), 0, res);
		while(itb.hasNext()) res = accumulator.accumulateIntPair(0, itb.nextInt(), res);
		return res;
	}
	
	public static double accumulate(double[] a, DoubleAccumulator accumulator) { return accumulateDoubles(a, accumulator); }
	public static double accumulate(DoubleIterable a, DoubleAccumulator accumulator) { return accumulateDoubles(a, accumulator); }
	public static float accumulate(float[] a, FloatAccumulator accumulator) { return accumulateFloats(a, accumulator); }
	public static float accumulate(FloatIterable a, FloatAccumulator accumulator) { return accumulateFloats(a, accumulator); }
	public static int accumulate(int[] a, IntAccumulator accumulator) { return accumulateInts(a, accumulator); }
	public static int accumulate(IntIterable a, IntAccumulator accumulator) { return accumulateInts(a, accumulator); }
	public static double accumulate(double[] a, double[] b, DoublePairAccumulator accumulator) { return accumulateDoublePairs(a, b, accumulator); }
	public static double accumulate(DoubleIterable a, DoubleIterable b, DoublePairAccumulator accumulator) { return accumulateDoublePairs(a, b, accumulator); }
	public static float accumulate(float[] a, float[] b, FloatPairAccumulator accumulator) { return accumulateFloatPairs(a, b, accumulator); }
	public static float accumulate(FloatIterable a, FloatIterable b, FloatPairAccumulator accumulator) { return accumulateFloatPairs(a, b, accumulator); }
	public static int accumulate(int[] a, int[] b, IntPairAccumulator accumulator) { return accumulateIntPairs(a, b, accumulator); }
	public static int accumulate(IntIterable a, IntIterable b, IntPairAccumulator accumulator) { return accumulateIntPairs(a, b, accumulator); }
	
	public static double variance(double[] vals) {
		double average = average(vals);
		return accumulateDoubles(vals, (x, old) -> old + square(x - average)) / vals.length;
	}
	
	public static double minDouble(double[] vals) { return accumulateDoubles(vals, Math::min); }
	public static double minDouble(DoubleIterable vals) { return accumulateDoubles(vals, Math::min); }
	public static double maxDouble(double[] vals) { return accumulateDoubles(vals, Math::max); }
	public static double maxDouble(DoubleIterable vals) { return accumulateDoubles(vals, Math::max); }
	public static float minFloat(float[] vals) { return accumulateFloats(vals, Math::min); }
	public static float minFloat(FloatIterable vals) { return accumulateFloats(vals, Math::min); }
	public static float maxFloat(float[] vals) { return accumulateFloats(vals, Math::max); }
	public static float maxFloat(FloatIterable vals) { return accumulateFloats(vals, Math::max); }
	public static int minInt(int[] vals) { return accumulateInts(vals, Math::min); }
	public static int minInt(IntIterable vals) { return accumulateInts(vals, Math::min); }
	public static int maxInt(int[] vals) { return accumulateInts(vals, Math::max); }
	public static int maxInt(IntIterable vals) { return accumulateInts(vals, Math::max); }
	
	public static double min(double[] vals) { return minDouble(vals); }
	public static double min(DoubleIterable vals) { return minDouble(vals); }// TODO
	public static float min(FloatIterable vals) { return minFloat(vals); }
	public static float min(float[] vals) { return minFloat(vals); }
	public static int min(int[] vals) { return minInt(vals); }
	public static int min(IntIterable vals) { return minInt(vals); }
	
	public static double sumDoubles(double[] vals) { return accumulateDoubles(vals, Double::sum); }
	public static double sumDoubles(DoubleIterable vals) { return accumulateDoubles(vals, Double::sum); }
	public static float sumFloats(float[] vals) { return accumulateFloats(vals, Float::sum); }
	public static float sumFloats(FloatIterable vals) { return accumulateFloats(vals, Float::sum); }
	public static int sumInts(int[] vals) { return accumulateInts(vals, Integer::sum); }
	public static int sumInts(IntIterable vals) { return accumulateInts(vals, Integer::sum); }
	
	public static double sum(DoubleIterable vals) { return sumDoubles(vals); }
	public static double sum(double[] vals) { return sumDoubles(vals); }
	public static float sum(float[] vals) { return sumFloats(vals); }
	public static float sum(FloatIterable vals) { return sumFloats(vals); }
	public static int sum(int[] vals) { return sumInts(vals); }
	public static int sum(IntIterable vals) { return sumInts(vals); }
	
	public static double norm1Double(double[] vals) { return accumulateDoubles(vals, (x, old) -> Math.abs(x) + old); }
	public static double norm1Double(DoubleIterable vals) { return accumulateDoubles(vals, (x, old) -> Math.abs(x) + old); }
	public static float norm1Float(float[] vals) { return accumulateFloats(vals, (x, old) -> Math.abs(x) + old); }
	public static float norm1Float(FloatIterable vals) { return accumulateFloats(vals, (x, old) -> Math.abs(x) + old); }
	public static int norm1Int(int[] vals) { return accumulateInts(vals, (x, old) -> Math.abs(x) + old); }
	public static int norm1Int(IntIterable vals) { return accumulateInts(vals, (x, old) -> Math.abs(x) + old); }
	public static double norm2SquaredDouble(double[] vals) { return accumulateDoubles(vals, (x, old) -> x * x + old); }
	public static double norm2SquaredDouble(DoubleIterable vals) { return accumulateDoubles(vals, (x, old) -> x * x + old); }
	public static float norm2SquaredFloat(float[] vals) { return accumulateFloats(vals, (x, old) -> x * x + old); }
	public static float norm2SquaredFloat(FloatIterable vals) { return accumulateFloats(vals, (x, old) -> x * x + old); }
	public static float norm2SquaredInt(int[] vals) { return accumulateInts(vals, (x, old) -> x * x + old); }
	public static float norm2SquaredInt(IntIterable vals) { return accumulateInts(vals, (x, old) -> x * x + old); }
	public static double norm2Double(double[] vals) { return Math.sqrt(norm2SquaredDouble(vals)); }
	public static double norm2Double(DoubleIterable vals) { return Math.sqrt(norm2SquaredDouble(vals)); }
	public static float norm2Float(float[] vals) { return MathHelper.sqrt(norm2SquaredFloat(vals)); }
	public static float norm2Float(FloatIterable vals) { return MathHelper.sqrt(norm2SquaredFloat(vals)); }
	public static double normInfDouble(double[] vals) { return accumulateDoubles(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	public static double normInfDouble(DoubleIterable vals) { return accumulateDoubles(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	public static float normInfFloat(float[] vals) { return accumulateFloats(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	public static float normInfFloat(FloatIterable vals) { return accumulateFloats(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	public static int normInfInt(int[] vals) { return accumulateInts(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	public static int normInfInt(IntIterable vals) { return accumulateInts(vals, (x, old) -> Math.max(Math.abs(x), old)); }
	
	public static double norm1(double[] vals) { return norm1Double(vals); }
	public static double norm1(DoubleIterable vals) { return norm1Double(vals); }
	public static float norm1(float[] vals) { return norm1Float(vals); }
	public static float norm1(FloatIterable vals) { return norm1Float(vals); }
	public static int norm1(int[] vals) { return norm1Int(vals); }
	public static int norm1(IntIterable vals) { return norm1Int(vals); }
	public static double norm2Squared(double[] vals) { return norm2SquaredDouble(vals); }
	public static double norm2Squared(DoubleIterable vals) { return norm2SquaredDouble(vals); }
	public static float norm2Squared(float[] vals) { return norm2SquaredFloat(vals); }
	public static float norm2Squared(FloatIterable vals) { return norm2SquaredFloat(vals); }
	public static float norm2Squared(int[] vals) { return norm2SquaredInt(vals); }
	public static float norm2Squared(IntIterable vals) { return norm2SquaredInt(vals); }
	public static double norm2(double[] vals) { return norm2Double(vals); }
	public static double norm2(DoubleIterable vals) { return norm2Double(vals); }
	public static float norm2(float[] vals) { return norm2Float(vals); }
	public static float norm2(FloatIterable vals) { return norm2Float(vals); }
	public static double normInf(double[] vals) { return normInfDouble(vals); }
	public static double normInf(DoubleIterable vals) { return normInfDouble(vals); }
	public static float normInf(float[] vals) { return normInfFloat(vals); }
	public static float normInf(FloatIterable vals) { return normInfFloat(vals); }
	public static int normInf(int[] vals) { return normInfInt(vals); }
	public static int normInf(IntIterable vals) { return normInfInt(vals); }
	
	public static double distance1Double(double[] p1, double[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static double distance1Double(DoubleIterable p1, DoubleIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static float distance1Float(float[] p1, float[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static float distance1Float(FloatIterable p1, FloatIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static int distance1Int(int[] p1, int[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static int distance1Int(IntIterable p1, IntIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.abs(a - b) + old); }
	public static double distance2SquaredDouble(double[] p1, double[] p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static double distance2SquaredDouble(DoubleIterable p1, DoubleIterable p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static float distance2SquaredFloat(float[] p1, float[] p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static float distance2SquaredFloat(FloatIterable p1, FloatIterable p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static float distance2SquaredInt(int[] p1, int[] p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static float distance2SquaredInt(IntIterable p1, IntIterable p2) { return accumulate(p1, p2, (a, b, old) -> square(a - b) + old); }
	public static double distance2Double(double[] p1, double[] p2) { return Math.sqrt(distance2Squared(p1, p2)); }
	public static double distance2Double(DoubleIterable p1, DoubleIterable p2) { return Math.sqrt(distance2Squared(p1, p2)); }
	public static float distance2Float(float[] p1, float[] p2) { return MathHelper.sqrt(distance2Squared(p1, p2)); }
	public static float distance2Float(FloatIterable p1, FloatIterable p2) { return MathHelper.sqrt(distance2Squared(p1, p2)); }
	public static double distanceInfDouble(double[] p1, double[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	public static double distanceInfDouble(DoubleIterable p1, DoubleIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	public static float distanceInfFloat(float[] p1, float[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	public static float distanceInfFloat(FloatIterable p1, FloatIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	public static int distanceInfInt(int[] p1, int[] p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	public static int distanceInfInt(IntIterable p1, IntIterable p2) { return accumulate(p1, p2, (a, b, old) -> Math.max(Math.abs(a - b), old)); }
	
	public static double distance1(double[] p1, double[] p2) { return distance1Double(p1, p2); }
	public static double distance1(DoubleIterable p1, DoubleIterable p2) { return distance1Double(p1, p2); }
	public static float distance1(float[] p1, float[] p2) { return distance1Float(p1, p2); }
	public static float distance1(FloatIterable p1, FloatIterable p2) { return distance1Float(p1, p2); }
	public static int distance1(int[] p1, int[] p2) { return distance1Int(p1, p2); }
	public static int distance1(IntIterable p1, IntIterable p2) { return distance1Int(p1, p2); }
	public static double distance2Squared(double[] p1, double[] p2) { return distance2SquaredDouble(p1, p2); }
	public static double distance2Squared(DoubleIterable p1, DoubleIterable p2) { return distance2SquaredDouble(p1, p2); }
	public static float distance2Squared(float[] p1, float[] p2) { return distance2SquaredFloat(p1, p2); }
	public static float distance2Squared(FloatIterable p1, FloatIterable p2) { return distance2SquaredFloat(p1, p2); }
	public static float distance2Squared(int[] p1, int[] p2) { return distance2SquaredInt(p1, p2); }
	public static float distance2Squared(IntIterable p1, IntIterable p2) { return distance2SquaredInt(p1, p2); }
	public static double distance2(double[] p1, double[] p2) { return distance2Double(p1, p2); }
	public static double distance2(DoubleIterable p1, DoubleIterable p2) { return distance2Double(p1, p2); }
	public static float distance2(float[] p1, float[] p2) { return distance2Float(p1, p2); }
	public static float distance2(FloatIterable p1, FloatIterable p2) { return distance2Float(p1, p2); }
	public static double distanceInf(double[] p1, double[] p2) { return distanceInfDouble(p1, p2); }
	public static double distanceInf(DoubleIterable p1, DoubleIterable p2) { return distanceInfDouble(p1, p2); }
	public static float distanceInf(float[] p1, float[] p2) { return distanceInfFloat(p1, p2); }
	public static float distanceInf(FloatIterable p1, FloatIterable p2) { return distanceInfFloat(p1, p2); }
	public static int distanceInf(int[] p1, int[] p2) { return distanceInfInt(p1, p2); }
	public static int distanceInf(IntIterable p1, IntIterable p2) { return distanceInfInt(p1, p2); }
	
	public static double averageDoubles(double[] vals) { return sumDoubles(vals) / vals.length; }
	public static double averageDoubles(DoubleIterable vals) {
		double sum = 0; int count = 0;
		DoubleIterator it = vals.iterator();
		while (it.hasNext()) {
			sum += it.nextDouble();
			++count;
		}
		return sum / count;
	}
	public static float averageFloats(float[] vals) { return sum(vals) / vals.length; }
	public static float averageFloats(FloatIterable vals) {
		float sum = 0; int count = 0;
		FloatIterator it = vals.iterator();
		while (it.hasNext()) {
			sum += it.nextFloat();
			++count;
		}
		return sum / count;
	}
	
	public static double average(double[] vals) { return averageDoubles(vals); }
	public static double average(DoubleIterable vals) { return averageDoubles(vals); }
	public static float average(float[] vals) { return averageFloats(vals); }
	public static float average(FloatIterable vals) { return averageFloats(vals); }
	
	public static double[] selfApplyDoubles(double[] vals, Double2DoubleFunction function) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.applyAsDouble(vals[i]);
		return vals;
	}
	public static float[] selfApplyFloats(float[] vals, Float2FloatFunction function) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.get(vals[i]);
		return vals;
	}
	public static int[] selfApplyInts(int[] vals, Int2IntFunction function) {
		for (int i = 0; i < vals.length; i++) vals[i] = function.applyAsInt(vals[i]);
		return vals;
	}
	
	public static double[] selfApply(double[] vals, Double2DoubleFunction function) { return selfApplyDoubles(vals, function); }
	public static float[] selfApply(float[] vals, Float2FloatFunction function) { return selfApplyFloats(vals, function); }
	public static int[] selfApply(int[] vals, Int2IntFunction function) { return selfApplyInts(vals, function); }
	
	public static double[] applyDoubles(double[] vals, Double2DoubleFunction function) { return selfApplyDoubles(vals.clone(), function); }
	public static float[] applyFloats(float[] vals, Float2FloatFunction function) { return selfApplyFloats(vals.clone(), function); }
	public static int[] applyInts(int[] vals, Int2IntFunction function) { return selfApplyInts(vals.clone(), function); }
	
	public static double[] apply(double[] vals, Double2DoubleFunction function) { return applyDoubles(vals, function); }
	public static float[] apply(float[] vals, Float2FloatFunction function) { return applyFloats(vals, function); }
	public static int[] apply(int[] vals, Int2IntFunction function) { return applyInts(vals, function); }
	
	public static double[] addDoubles(double[] a, double[] b) {
		double[] res = Arrays.copyOf(a, Math.max(a.length, b.length));
		for(int i = 0; i < b.length; ++i) res[i] += b[i];
		return res;
	}
	public static float[] addFloats(float[] a, float[] b) {
		float[] res = Arrays.copyOf(a, Math.max(a.length, b.length));
		for(int i = 0; i < b.length; ++i) res[i] += b[i];
		return res;
	}
	public static int[] addInts(int[] a, int[] b) {
		int[] res = Arrays.copyOf(a, Math.max(a.length, b.length));
		for(int i = 0; i < b.length; ++i) res[i] += b[i];
		return res;
	}
	public static double[] add(double[] a, double[] b) { return addDoubles(a, b); }
	public static float[] add(float[] a, float[] b) { return addFloats(a, b); }
	public static int[] add(int[] a, int[] b) { return addInts(a, b); }
	
	public static double[] selfReverseOrderDoubles(double[] arr) {
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
	public static float[] selfReverseOrderFloats(float[] arr) {
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
	public static int[] selfReverseOrderInts(int[] arr) {
		int i = 0, j = arr.length - 1;
		while (i < j) {
			int tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
			++i;
			--j;
		}
		return arr;
	}
	
	public static double[] selfReverseOrder(double[] arr) { return selfReverseOrderDoubles(arr); }
	public static float[] selfReverseOrder(float[] arr) { return selfReverseOrderFloats(arr); }
	public static int[] selfReverseOrder(int[] arr) { return selfReverseOrderInts(arr); }
	
	public static double[] apply(int[] vals, Int2DoubleFunction function) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
	public static <T> double[] apply(T[] vals, ToDoubleFunction<T> function) {
		double[] result = new double[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsDouble(vals[i]);
		return result;
	}
	public static <T> float[] toFloatApply(T[] vals, ToFloatFunction<T> function) {
		float[] result = new float[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsFloat(vals[i]);
		return result;
	}
	public static <T> int[] toIntApply(T[] vals, ToIntFunction<T> function) {
		int[] result = new int[vals.length];
		for (int i = 0; i < vals.length; i++) result[i] = function.applyAsInt(vals[i]);
		return result;
	}
	public static <T> int[] apply(T[] vals, ToIntFunction<T> function) {
		return toIntApply(vals, function);
	}
	public static <T> float[] apply(T[] vals, ToFloatFunction<T> function) {
		return toFloatApply(vals, function);
	}
	public static double[] convolution(double[] a, double[] b) {
		double[] result = new double[a.length + b.length - 1];
		for(int i = 0; i < a.length; ++i)
			for(int j = 0; j < b.length; ++j)
				result[i + j] += a[i] * b[j];
		return result;
	}
	public static float[] convolution(float[] a, float[] b) {
		float[] result = new float[a.length + b.length - 1];
		for(int i = 0; i < a.length; ++i)
			for(int j = 0; j < b.length; ++j)
				result[i + j] = Math.fma(a[i], b[j], result[i + j]);
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
	
	// 将a与flattenWidth个1.0f/flattenWidth组成的卷积核进行卷积
	public static double[] fastConvoluteFlat(double[] a, int flattenWidth) {
		if(flattenWidth < 1) throw new IllegalArgumentException();
		double[] res = new double[a.length + flattenWidth - 1];
		double[] buf = new double[res.length];
		System.arraycopy(a, 0, buf, 0, a.length);
		int k = 1;
		int i = 0;
		while(true) {
			if((flattenWidth & k) != 0) {
				for(int j = 0; j < a.length + k - 1; ++j)
					res[j + i] += buf[j];
				i |= k;
			}
			if((k << 1) > flattenWidth) break;
			for(int j = a.length + (k << 1) - 1; --j >= k;)
				buf[j] += buf[j - k];
			k <<= 1;
		}
		double k1 = 1.0 / flattenWidth;
		for(int j = 0; j < res.length; ++j)
			res[j] *= k1;
		return res;
	}
	public static float[] fastConvoluteFlat(float[] a, int flattenWidth) {
		if(flattenWidth < 1) throw new IllegalArgumentException();
		float[] res = new float[a.length + flattenWidth - 1];
		float[] buf = new float[res.length];
		System.arraycopy(a, 0, buf, 0, a.length);
		int k = 1;
		int i = 0;
		while(true) {
			if((flattenWidth & k) != 0) {
				for(int j = 0; j < a.length + k - 1; ++j)
					res[j + i] += buf[j];
				i |= k;
			}
			if((k << 1) > flattenWidth) break;
			for(int j = a.length + (k << 1) - 1; --j >= k;)
				buf[j] += buf[j - k];
			k <<= 1;
		}
		float k1 = 1.0f / flattenWidth;
		for(int j = 0; j < res.length; ++j)
			res[j] *= k1;
		return res;
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
	
	public static <T> int max(T[] values, ToIntFunction<T> function) {
		int maxValue = Integer.MIN_VALUE;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsInt(value));
		return maxValue;
	}
	
	public static <T> int intMax(Iterable<T> values, ToIntFunction<T> function) {
		int maxValue = Integer.MIN_VALUE;
		for (T value : values) maxValue = Math.max(maxValue, function.applyAsInt(value));
		return maxValue;
	}
	
	public interface ModdedPostProcessing {
		float apply(float a, int iPeriod);
	}
}
