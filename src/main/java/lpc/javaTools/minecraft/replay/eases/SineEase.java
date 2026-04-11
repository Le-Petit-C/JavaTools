package lpc.javaTools.minecraft.replay.eases;

import lpc.javaTools.utils.MathUtils;
import lpc.javaTools.minecraft.replay.Ease;

public class SineEase implements Ease {
	private final double start, end;
	private final double startVal, endVal;
	public SineEase(double start, double end) {
		if(start > end) throw new IllegalArgumentException("start > end");
		if(start < -1) throw new IllegalArgumentException("start < -1");
		if(end > 1) throw new IllegalArgumentException("end > 1");
		this.start = start * Math.PI / 2;
		this.end = end * Math.PI / 2;
		this.startVal = Math.sin(start);
		this.endVal = Math.sin(end);
	}
	public SineEase(){
		this(-1, 1);
	}
	@Override public double translate(double val) {
		if(startVal == endVal) return val;
		else return MathUtils.unlerp(startVal, endVal, Math.sin(MathUtils.lerp(start, end, val)));
	}
}
