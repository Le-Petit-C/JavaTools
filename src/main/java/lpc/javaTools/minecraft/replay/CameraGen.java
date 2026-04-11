package lpc.javaTools.minecraft.replay;

import org.joml.Vector3d;

public interface CameraGen {
	// time也是从0到1的数，表示当前帧在整个动画中的位置
	Camera generate(Camera camera, double time);
	
	default CameraGen ease(Ease ease) {
		return (camera, time) -> generate(camera, ease.translate(time));
	}
	
	default CameraGen focus(Vector3d target) {
		return (camera, time) -> generate(camera, time).lookAt(target);
	}
}
