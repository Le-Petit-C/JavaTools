package lpc.javaTools.minecraft.replay;

import lpc.javaTools.utils.MathUtils;
import org.joml.Vector3d;

public interface CameraGen {
	// time是从0到1的数，表示当前帧在整个动画中的位置
	Camera generate(Camera camera, double time);
	
	default CameraGen ease(Ease ease) {
		return (camera, time) -> generate(camera, ease.translate(time));
	}
	
	default CameraGen focus(Vector3d target) {
		return (camera, time) -> generate(camera, time).lookAt(target);
	}
	
	default CameraGen transformOpen(CamTransform transform, double startTime, double endTime) {
		if(startTime >= endTime) throw new IllegalArgumentException("startTime >= endTime");
		if(startTime < 0) throw new IllegalArgumentException("startTime < 0");
		if(endTime > 1) throw new IllegalArgumentException("endTime > 1");
		return (camera, time) -> {
			var cam = generate(camera, time);
			if(time > startTime && time < endTime) return transform.transform(cam, MathUtils.unlerp(startTime, endTime, time));
			else return cam;
		};
	}
	
	default CameraGen transformClose(CamTransform transform, double startTime, double endTime) {
		if(startTime >= endTime) throw new IllegalArgumentException("startTime >= endTime");
		if(startTime < 0) throw new IllegalArgumentException("startTime < 0");
		if(endTime > 1) throw new IllegalArgumentException("endTime > 1");
		return (camera, time) -> {
			var cam = generate(camera, time);
			if(time >= startTime && time <= endTime) return transform.transform(cam, MathUtils.unlerp(startTime, endTime, time));
			else return cam;
		};
	}
	
	default CameraGen transformOC(CamTransform transform, double startTime, double endTime) {
		if(startTime >= endTime) throw new IllegalArgumentException("startTime >= endTime");
		if(startTime < 0) throw new IllegalArgumentException("startTime < 0");
		if(endTime > 1) throw new IllegalArgumentException("endTime > 1");
		return (camera, time) -> {
			var cam = generate(camera, time);
			if(time > startTime && time <= endTime) return transform.transform(cam, MathUtils.unlerp(startTime, endTime, time));
			else return cam;
		};
	}
	
	default CameraGen transformCO(CamTransform transform, double startTime, double endTime) {
		if(startTime >= endTime) throw new IllegalArgumentException("startTime >= endTime");
		if(startTime < 0) throw new IllegalArgumentException("startTime < 0");
		if(endTime > 1) throw new IllegalArgumentException("endTime > 1");
		return (camera, time) -> {
			var cam = generate(camera, time);
			if(time >= startTime && time < endTime) return transform.transform(cam, MathUtils.unlerp(startTime, endTime, time));
			else return cam;
		};
	}
	
	default CameraGen transform(CamTransform transform) {
		return (camera, time) -> transform.transform(generate(camera, time), time);
	}
}
