package lpc.javaTools.utils.math.eases;

import lpc.javaTools.minecraft.replay.Ease;

public class LinearEase implements Ease {
	public static final LinearEase instance = new LinearEase();
	@Override public double translate(double val) { return val; }
	private LinearEase() {}
}
