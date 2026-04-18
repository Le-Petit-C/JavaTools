package lpc.javaTools.minecraft.replay.camTransforms;

import lpc.javaTools.minecraft.replay.CamTransform;
import lpc.javaTools.minecraft.replay.Camera;

public class LookBehind implements CamTransform {
	@Override public Camera transform(Camera camera, double time) {
		return camera.applyRotate(rotation->rotation.rotateY(Math.PI));
	}
}
