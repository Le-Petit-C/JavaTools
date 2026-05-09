package lpc.javaTools.utils.math;

import lpc.javaTools.utils.SystemInfo;
import lpc.javaTools.utils.math.sampledMath.ClampedFloatSampledMath;
import lpc.javaTools.utils.math.sampledMath.PeriodicFloatSampledMath_ProcessReturn;
import org.apache.commons.math3.distribution.NormalDistribution;

@SuppressWarnings("unused")
public class MathHelper {
	public static double PI = Math.PI;
	public static float PI_F = (float)Math.PI;
	
	// 黄金分割率
	public static double FAI = 2.0 / (Math.sqrt(5) + 1);
	public static float FAI_F = (float) FAI;
	
	public static int floor(float a) {
		int i = (int) a;
		if(i > a) return i - 1;
		else return i;
	}
	public static int ceil(float a) {
		int i = (int) a;
		if(i < a) return i + 1;
		else return i;
	}
	public static double floorMod(double a, double b) {
		double r = a % b;
		if((r < 0) != (b < 0)) return r + b;
		else return r;
	}
	public static float floorMod(float a, float b) {
		float r = a % b;
		if((r < 0) != (b < 0)) return r + b;
		else return r;
	}
	public static double checkedFloorMod(double a, double b) {
		if((a >= 0 && a < b) || (a <= 0 && a > b)) return a;
		double r = a % b;
		if((r < 0) != (b < 0)) return r + b;
		else return r;
	}
	public static float checkedFloorMod(float a, float b) {
		if((a >= 0 && a < b) || (a <= 0 && a > b)) return a;
		float r = a % b;
		if((r < 0) != (b < 0)) return r + b;
		else return r;
	}
	
	// regard p as unsigned
	public static float fastPowUnsigned(float x, int p) {
		float res = 1;
		while(true) {
			if((p & 1) != 0) res *= x;
			if((p >>>= 1) == 0) break;
			x *= x;
		}
		return res;
	}
	
	public static float fastPow(float x, int p) {
		if(p >= 0) return fastPowUnsigned(x, p);
		else {
			float xInv = 1.0f / x;
			return xInv * fastPowUnsigned(xInv, ~p);
		}
	}
	
	public static float normalDCP(float x) { return SampledNormalDCP.instance.quad(x); }
	public static float sqrt(float x) { return org.joml.Math.sqrt(x); }
	public static float exp(float x) { return SampledExp.instance.quad(x); }
	public static float pow(float x, float y) { return (float) Math.pow(x, y); }
	public static float sin(float x) { return org.joml.Math.sin(x); }
	public static float cos(float x) { return org.joml.Math.cos(x); }
	public static float tan(float x) { return org.joml.Math.tan(x); }
	
	private static class SampledNormalDCP {
		static final ClampedFloatSampledMath instance;
		static {
			NormalDistribution normalDistribution = new NormalDistribution();
			float step = 1.0f / (1 << 11);
			float min = 0, max = 0;
			while((float)normalDistribution.cumulativeProbability(min) != 0.0f) min -= step;
			while((float)normalDistribution.cumulativeProbability(max) != 1.0f) max += step;
			instance = new ClampedFloatSampledMath(min, max, step, normalDistribution::cumulativeProbability);
		}
	}
	
	private static class SampledExp {
		static final PeriodicFloatSampledMath_ProcessReturn instance;
		static {
			NormalDistribution normalDistribution = new NormalDistribution();
			float step = 1.0f / (1 << 11);
			float period = (float)Math.log(2);
			instance = new PeriodicFloatSampledMath_ProcessReturn(period, step, (x, i)->x * fastPow(2.0f, i), Math::exp);
		}
	}
	
	/** @see org.joml.Options */
	private static void staticInit() {
		System.setProperty("joml.fastmath", "true");
		System.setProperty("joml.sinLookup", "true");
		if(SystemInfo.FMASupport())
			System.getProperty("joml.useMathFma", "true");
	}
	static { staticInit(); }
	
}
