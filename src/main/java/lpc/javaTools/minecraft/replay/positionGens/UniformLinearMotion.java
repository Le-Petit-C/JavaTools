package lpc.javaTools.minecraft.replay.positionGens;

import lpc.javaTools.minecraft.replay.PositionGen;
import org.joml.Vector3d;

public class UniformLinearMotion implements PositionGen {
	private final Vector3d start;
	private final Vector3d end;
	
	public UniformLinearMotion(Vector3d start, Vector3d end) {
		this.start = start;
		this.end = end;
	}
	
	@Override public Vector3d generate(Vector3d res, double time) {
		return end.sub(start, res).mul(time).add(start);
	}
}