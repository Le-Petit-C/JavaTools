package lpc.javaTools.minecraft.replay.positionGens;

import lpc.javaTools.minecraft.replay.PositionGen;
import org.joml.Vector3d;

public class UniformBSpline implements PositionGen {
	
	private final Vector3d[] controlPoints;
	private final int degree; // n 次（degree）
	private final double[] knots;
	
	public UniformBSpline(Vector3d[] controlPoints, int degree) {
		if (controlPoints.length < degree + 1) {
			throw new IllegalArgumentException("控制点数量必须 >= degree + 1");
		}
		this.controlPoints = controlPoints;
		this.degree = degree;
		this.knots = createUniformKnotVector(controlPoints.length, degree);
	}
	
	@Override
	public Vector3d generate(Vector3d res, double time) {
		int n = controlPoints.length - 1;
		
		// 映射到 knot 范围
		double tMin = knots[degree];
		double tMax = knots[n + 1];
		double t = tMin + time * (tMax - tMin);
		
		res.zero();
		
		for (int i = 0; i <= n; i++) {
			double basis = basis(i, degree, t);
			if (basis != 0.0) {
				res.fma(basis, controlPoints[i]); // res += basis * point
			}
		}
		
		return res;
	}
	
	// Cox–de Boor 递推
	private double basis(int i, int k, double t) {
		if (k == 0) {
			return (knots[i] <= t && t < knots[i + 1]) ? 1.0 : 0.0;
		}
		
		double denom1 = knots[i + k] - knots[i];
		double denom2 = knots[i + k + 1] - knots[i + 1];
		
		double term1 = 0.0;
		double term2 = 0.0;
		
		if (denom1 != 0) {
			term1 = (t - knots[i]) / denom1 * basis(i, k - 1, t);
		}
		if (denom2 != 0) {
			term2 = (knots[i + k + 1] - t) / denom2 * basis(i + 1, k - 1, t);
		}
		
		return term1 + term2;
	}
	
	// 均匀节点向量
	private double[] createUniformKnotVector(int numPoints, int degree) {
		int m = numPoints + degree + 1;
		double[] knots = new double[m];
		
		for (int i = 0; i < m; i++) {
			knots[i] = i;
		}
		
		return knots;
	}
}