package lpc.javaTools.utils.math.sampledMath;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import lpc.javaTools.utils.math.MathHelper;

public class PeriodicFloatSampledMath extends RawFloatSampledMath {
	private final float period;
	
	public PeriodicFloatSampledMath(float period, float step, Double2DoubleFunction function) {
		super(-step, period + step * 1.5f, step, function);
		this.period = period;
	}
	
	@Override public float finiteLinear(float x) { return rawLinear(MathHelper.checkedFloorMod(x, period)); }
	@Override public float finiteQuad(float x) { return rawQuad(MathHelper.checkedFloorMod(x, period)); }
}
