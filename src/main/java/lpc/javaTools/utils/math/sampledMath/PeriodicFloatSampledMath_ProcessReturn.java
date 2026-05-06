package lpc.javaTools.utils.math.sampledMath;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import lpc.javaTools.utils.math.MathHelper;
import lpc.javaTools.utils.math.MathUtils;

public class PeriodicFloatSampledMath_ProcessReturn extends RawFloatSampledMath {
	private final float period;
	private final MathUtils.ModdedPostProcessing process;
	private final Float2FloatFunction rawLinear = super::rawLinear;
	private final Float2FloatFunction rawQuad = super::rawQuad;
	public PeriodicFloatSampledMath_ProcessReturn(float period, float step, MathUtils.ModdedPostProcessing process, Double2DoubleFunction function) {
		super(-step, period + step * 1.5f, step, function);
		this.period = period;
		this.process = process;
	}
	
	private float processPeriod(float x, Float2FloatFunction function) {
		int iPeriod;
		float periodX;
		if (0 <= x && x < period) {
			iPeriod = 0;
			periodX = x;
		} else {
			iPeriod = MathHelper.floor(x / period);
			periodX = x - iPeriod * period;
		}
		return process.apply(function.get(periodX), iPeriod);
	}
	
	@Override public float finiteLinear(float x) { return processPeriod(x, rawLinear); }
	@Override public float finiteQuad(float x) { return processPeriod(x, rawQuad); }
}
