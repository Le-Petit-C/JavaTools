package lpc.javaTools.utils.math.sampledMath;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import lpc.javaTools.utils.math.MathHelper;
import lpc.javaTools.utils.math.MathUtils;

public abstract class RawFloatSampledMath implements FloatSampledMath {
	private final float[] samples;
	protected final float min, step, stepInv;
	protected final float minAbsX;
	protected final int minAbsXIndex;
	protected final float pInvY, nInfY;
	
	public RawFloatSampledMath(float min, float max, float step, Double2DoubleFunction function) {
		this.min = min;
		this.step = step;
		this.stepInv = 1.0f / step;
		FloatArrayList list = new FloatArrayList();
		int iMinXAbs = -1;
		float minAbsX = Float.MAX_VALUE;
		for (float x = min; x <= max; x += step) {
			if (Math.abs(x) < Math.abs(minAbsX)) {
				minAbsX = x;
				iMinXAbs = list.size();
			}
			list.add((float) function.applyAsDouble(x));
		}
		this.minAbsX = minAbsX;
		this.minAbsXIndex = iMinXAbs;
		this.pInvY = (float)function.applyAsDouble(Double.POSITIVE_INFINITY);
		this.nInfY = (float)function.applyAsDouble(Double.NEGATIVE_INFINITY);
		samples = list.toFloatArray();
	}
	
	@Override public float linear(float x) {
		if(!Float.isFinite(x)) return notFiniteValue(x);
		else return finiteLinear(x);
	}
	@Override public float quad(float x) {
		if(!Float.isFinite(x)) return notFiniteValue(x);
		else return finiteQuad(x);
	}
	
	abstract float finiteLinear(float x);
	abstract float finiteQuad(float x);
	
	protected float rawLinear(float x) {
		int i = MathHelper.floor((x - min) * stepInv);
		return MathUtils.lerp(samples[i], samples[i + 1], xAround(x, i));
	}
	
	protected float rawQuad(float x) {
		if (samples.length <= 2) return linear(x);
		int i = Math.round((x - min) * stepInv);
		return MathUtils.qerp(samples[i - 1], samples[i], samples[i + 1], xAround(x, i));
	}
	
	protected float xAround(float x, int i) { return (x - minAbsX) * stepInv - (i - minAbsXIndex); }
	protected float notFiniteValue(float x) {
		if(Float.isNaN(x)) return Float.NaN;
		else return x < 0 ? nInfY : pInvY;
	}
}
