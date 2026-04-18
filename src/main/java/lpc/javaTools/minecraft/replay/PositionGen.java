package lpc.javaTools.minecraft.replay;

import org.joml.Vector3d;

public interface PositionGen {
	// time是从0到1的数，表示当前帧在整个动画中的位置
	Vector3d generate(Vector3d res, double time);
	
	default PositionGen ease(Ease ease) {
		return (res, time) -> generate(res, ease.translate(time));
	}
}
