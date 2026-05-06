package lpc.javaTools.utils.math.sampledMath;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

public class ClampedFloatSampledMath extends RawFloatSampledMath {
	final float clampMin, clampMax;
	
	public ClampedFloatSampledMath(float min, float max, float step, Double2DoubleFunction function) {
		super(min - step, max + step * 1.5f, step, function);
		this.clampMin = min;
		this.clampMax = max;
	}
	
	@Override public float finiteLinear(float x) { return rawLinear(Math.clamp(x, clampMin, clampMax)); }
	@Override public float finiteQuad(float x) { return rawQuad(Math.clamp(x, clampMin, clampMax)); }
}
