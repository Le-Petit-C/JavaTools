package lpc.javaTools.minecraft.replay.positionGens;

import lpc.javaTools.minecraft.replay.PositionGen;
import org.joml.Vector3d;

public class NaturalCubicSpline implements PositionGen {
	
	private final Vector3d[] points;
	private final int n;
	
	// 二阶导数
	private final Vector3d[] M;
	
	public NaturalCubicSpline(Vector3d[] points) {
		if (points.length < 2) {
			throw new IllegalArgumentException("至少需要2个点");
		}
		this.points = points;
		this.n = points.length - 1;
		this.M = new Vector3d[points.length];
		
		for (int i = 0; i <= n; i++) {
			M[i] = new Vector3d();
		}
		
		computeSecondDerivatives();
	}
	
	// 解三对角方程，求二阶导
	private void computeSecondDerivatives() {
		int size = n + 1;
		
		double[] a = new double[size];
		double[] b = new double[size];
		double[] c = new double[size];
		
		Vector3d[] d = new Vector3d[size];
		for (int i = 0; i < size; i++) {
			d[i] = new Vector3d();
		}
		
		// 自然边界
		b[0] = 1;
		b[n] = 1;
		d[0].zero();
		d[n].zero();
		
		// 内部点
		for (int i = 1; i < n; i++) {
			a[i] = 1;
			b[i] = 4;
			c[i] = 1;
			
			Vector3d term = new Vector3d(points[i + 1])
				.sub(points[i].mul(2, new Vector3d()))
				.add(points[i - 1]);
			
			term.mul(3); // 因为 h=1
			
			d[i].set(term);
		}
		
		// Thomas算法解三对角
		for (int i = 1; i <= n; i++) {
			double w = a[i] / b[i - 1];
			b[i] -= w * c[i - 1];
			d[i].sub(new Vector3d(d[i - 1]).mul(w));
		}
		
		M[n].set(d[n]).div(b[n]);
		
		for (int i = n - 1; i >= 0; i--) {
			M[i].set(d[i])
				.sub(new Vector3d(M[i + 1]).mul(c[i]))
				.div(b[i]);
		}
	}
	
	@Override
	public Vector3d generate(Vector3d res, double time) {
		// 映射到区间
		double t = time * n;
		
		int i = Math.min((int) Math.floor(t), n - 1);
		double u = t - i;
		
		Vector3d p0 = points[i];
		Vector3d p1 = points[i + 1];
		
		Vector3d m0 = M[i];
		Vector3d m1 = M[i + 1];
		
		double u2 = u * u;
		double u3 = u2 * u;
		
		// 标准 cubic spline 公式
		Vector3d term1 = new Vector3d(p0).mul(1 - 3 * u2 + 2 * u3);
		Vector3d term2 = new Vector3d(p1).mul(3 * u2 - 2 * u3);
		Vector3d term3 = new Vector3d(m0).mul(u - 2 * u2 + u3);
		Vector3d term4 = new Vector3d(m1).mul(-u2 + u3);
		
		res.set(term1)
			.add(term2)
			.add(term3)
			.add(term4);
		
		return res;
	}
}