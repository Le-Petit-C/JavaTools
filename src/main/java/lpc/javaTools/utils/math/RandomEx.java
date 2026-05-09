package lpc.javaTools.utils.math;

import java.util.Random;

public class RandomEx extends Random {
	public RandomEx() { super(); }
	public RandomEx(long seed) { super(seed); }
	
	public double[] nextDoubleArray(double[] res) {
		for(int i = 0; i < res.length; ++i) res[i] = nextDouble();
		return res;
	}
	
	public double[] nextDoubleArray(int n) { return nextDoubleArray(new double[n]); }
	public double nextGaussian(double k) { return k * nextGaussian(); }
	// TODO 原生floatGaussian而不是从double强制转换
	public float nextFloatGaussian() { return (float)nextGaussian(); }
	public float nextFloatGaussian(float k) { return k * (float)nextGaussian(); }
	
	public double[] nextGaussianArray(double[] res) {
		for(int i = 0; i < res.length; ++i) res[i] = nextGaussian();
		return res;
	}
	public float[] nextFloatGaussianArray(float[] res) {
		for(int i = 0; i < res.length; ++i) res[i] = nextFloatGaussian();
		return res;
	}
	public double[] nextGaussianArray(int n) { return nextGaussianArray(new double[n]); }
	public float[] nextFloatGaussianArray(int n) { return nextFloatGaussianArray(new float[n]); }
	
	public double nextDoubleEx() {
		int shift = 0;
		
		while(true) {
			int n = nextInt();
			if(n != 0) {
				shift += Integer.numberOfTrailingZeros(n);
				break;
			}
			shift += 32;
		}
		
		return Math.scalb(
			super.nextDouble() * 0.5 + 0.5,
			-shift
		);
	}
	
	public float nextFloatEx() {
		int shift = 0;
		
		while(true) {
			int n = nextInt();
			if(n != 0) {
				shift += Integer.numberOfTrailingZeros(n);
				break;
			}
			shift += 32;
		}
		
		return Math.scalb(
			super.nextFloat() * 0.5f + 0.5f,
			-shift
		);
	}
}
