package lpc.javaTools.minecraft.replay.cameraGens;

import lpc.javaTools.minecraft.replay.Camera;
import lpc.javaTools.minecraft.replay.CameraGen;
import lpc.javaTools.minecraft.replay.Ease;
import lpc.javaTools.utils.math.eases.LinearEase;
import org.joml.Vector3d;

public class CylinderSurround implements CameraGen {
	private final Vector3d center;
	private final Vector3d axis;
	private final Vector3d start; // 起点相对于中心的位置
	private final double angle;
	private final double axisDrift;
	private final Ease driftEase;
	
	public CylinderSurround(Vector3d center, Vector3d axis, Vector3d start, double angle, double axisDrift, Ease driftEase) {
		this.center = center;
		this.axis = axis.normalize(new Vector3d());
		this.start = new Vector3d(start);
		this.angle = angle;
		this.axisDrift = axisDrift;
		this.driftEase = driftEase;
	}
	
	public CylinderSurround(Vector3d center, Vector3d axis, Vector3d start, double angle, double axisDrift) {
		this(center, axis, start, angle, axisDrift, LinearEase.instance);
	}
	
	public CylinderSurround(Vector3d center, Vector3d axis, Vector3d start, double angle) {
		this(center, axis, start, angle, 0, LinearEase.instance);
	}
	
	@Override public Camera generate(Camera camera, double time) {
		var pos = camera.position.set(start);
		pos.rotateAxis(angle * time, axis.x, axis.y, axis.z);
		pos.fma(driftEase.translate(time) * axisDrift, axis);
		pos.add(center);
		return camera.lookAt(center, axis);
	}
}
