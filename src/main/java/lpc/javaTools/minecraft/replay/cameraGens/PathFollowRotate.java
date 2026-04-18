package lpc.javaTools.minecraft.replay.cameraGens;

import lpc.javaTools.minecraft.replay.Camera;
import lpc.javaTools.minecraft.replay.CameraGen;
import lpc.javaTools.minecraft.replay.PositionGen;
import org.joml.Vector3d;

public class PathFollowRotate implements CameraGen {
	private final Camera[] samples;
	public PathFollowRotate(PositionGen positionGen, Vector3d startUpDirection, double startRotate, double dt, int steps, double rotateSpeed) {
		samples = new Camera[steps + 1];
		Vector3d nextDirection = new Vector3d();
		Vector3d pos1 = new Vector3d();
		Vector3d pos2 = new Vector3d();
		double rotateStep = rotateSpeed / steps;
		for(int i = 0; i <= steps; ++i) {
			Camera camera = samples[i] = new Camera();
			double curr = (double)i / steps;
			double _dt = dt;
			while(true) {
				positionGen.generate(pos1, curr - curr * _dt);
				positionGen.generate(pos2, curr + (1 - curr) * _dt);
				pos2.sub(pos1, nextDirection);
				nextDirection.normalize();
				if(nextDirection.isFinite()) break;
				_dt *= 2;
			}
			positionGen.generate(camera.position, curr);
			if(i == 0) {
				camera.lookAt(camera.position.add(nextDirection, pos2), startUpDirection);
				camera.rotation.rotateAxis(startRotate, new Vector3d(0, 0, 1));
				//camera.lookAtDirection(nextDirection);
			}
			else {
				camera.rotation.set(samples[i - 1].rotation);
				camera.lookAtDirection(nextDirection);
				camera.rotation.rotateAxis(rotateStep, new Vector3d(0, 0, 1));
			}
		}
	}
	public PathFollowRotate(PositionGen positionGen, Vector3d startUpDirection, double startRotate, int steps, double rotateSpeed) {
		this(positionGen, startUpDirection, startRotate, 1e-7, steps, rotateSpeed);
	}
	public PathFollowRotate(PositionGen positionGen, Vector3d startUpDirection, double startRotate, double rotateSpeed) {
		this(positionGen, startUpDirection, startRotate, 1e-7, 1024, rotateSpeed);
	}
	@Override public Camera generate(Camera camera, double time) {
		int n = samples.length - 1;
		double _t = time * n;
		int i1 = (int) Math.floor(_t);
		double t = _t - i1;
		if(t == 0) return camera.set(samples[i1]);
		Camera cam1 = samples[i1];
		Camera cam2 = samples[i1 + 1];
		cam2.position.sub(cam1.position, camera.position).mul(t).add(cam1.position);
		cam1.rotation.slerp(cam2.rotation, t, camera.rotation);
		return camera;
	}
}
