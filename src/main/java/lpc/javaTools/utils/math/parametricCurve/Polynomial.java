package lpc.javaTools.utils.math.parametricCurve;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

public class Polynomial implements Double2DoubleFunction {
	// 设a=coefficients，约定多项式为\sum_{i=0}^{a.length-1}a_{i}x^{i}
	public double[] coefficients;
	public Polynomial(double[] coefficients) {
		this.coefficients = coefficients;
	}
	@Override public double get(double x) {
		int i = coefficients.length - 1;
		if(i >= 0) {
			double res = coefficients[i];
			while (--i >= 0) res = Math.fma(res, x, coefficients[i]);
			return res;
		}
		else return 0;
	}
	// 有横坐标相等(或插值结果过于极端)时并不会抛出异常而是会返回一个系数中可能带有nan或inf的Polynomial
	public static Polynomial newtonInterpolation(double[] x, double[] y) {
		if(x.length != y.length) throw new IllegalArgumentException();
		int n = x.length;
		if(n == 0) return new Polynomial(new double[0]);
		double[] differenceTable = y.clone();
		for(int i = 1; i < n; ++i) for(int j = n; --j >= i;)
			differenceTable[j] = (differenceTable[j] - differenceTable[j - 1]) / (x[j] - x[j - i]);
		double[] coefficients = new double[n];
		double[] coefficientsTemp = new double[n];
		coefficientsTemp[0] = 1;
		int i = 0;
		while(true) {
			double difference = differenceTable[i];
			for(int j = 0; j <= i; ++j) coefficients[j] += coefficientsTemp[j] * difference;
			double oxi = -x[i];
			if(++i >= n) break;
			for(int j = i; j > 0; --j) {
				coefficientsTemp[j] += coefficientsTemp[j - 1];
				coefficientsTemp[j - 1] *= oxi;
			}
		}
		return new Polynomial(coefficients);
	}
}
