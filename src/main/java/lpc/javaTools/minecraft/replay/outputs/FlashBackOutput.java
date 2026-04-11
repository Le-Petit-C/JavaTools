package lpc.javaTools.minecraft.replay.outputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lpc.javaTools.minecraft.replay.KeyFrame;
import lpc.javaTools.minecraft.replay.Output;
import org.joml.Vector3d;

import java.util.List;

public class FlashBackOutput implements Output {
	@Override public String generate(List<KeyFrame> keyFrames) {
		JsonArray savedTracks = new JsonArray();
		savedTracks.add(cameraTracks(keyFrames));
		savedTracks.add(timeElapseTracks(keyFrames));
		JsonObject result = new JsonObject();
		result.add("savedTracks", savedTracks);
		return result.toString();
	}
	
	private static JsonObject cameraTracks(List<KeyFrame> keyFrames) {
		JsonObject cameraTracks = new JsonObject();
		cameraTracks.addProperty("type", "CAMERA");
		cameraTracks.addProperty("track", 0);
		cameraTracks.addProperty("copiedFromDisabled", false);
		JsonObject keyframes = new JsonObject();
		for (KeyFrame kf : keyFrames) keyframes.add(String.valueOf(Math.round(kf.gameTime() * 20)), kfCameraJson(kf));
		cameraTracks.add("keyframes", keyframes);
		return cameraTracks;
	}
	
	private static JsonObject timeElapseTracks(List<KeyFrame> keyFrames) {
		JsonObject cameraTracks = new JsonObject();
		cameraTracks.addProperty("type", "TIMELAPSE");
		cameraTracks.addProperty("track", 1);
		cameraTracks.addProperty("copiedFromDisabled", false);
		JsonObject keyframes = new JsonObject();
		for (KeyFrame kf : keyFrames) {
			JsonObject elapseJson = new JsonObject();
			elapseJson.addProperty("ticks", Math.round(kf.videoTime() * 20));
			elapseJson.addProperty("type", "timelapse");
			keyframes.add(String.valueOf(Math.round(kf.gameTime() * 20)), elapseJson);
		}
		cameraTracks.add("keyframes", keyframes);
		return cameraTracks;
	}
	
	private static JsonObject kfCameraJson(KeyFrame kf) {
		var camera = kf.camera();
		JsonObject kfJson = new JsonObject();
		var position = new JsonArray();
		position.add(camera.position.x);
		position.add(camera.position.y);
		position.add(camera.position.z);
		kfJson.add("position", position);
		Vector3d eulerAngle = kf.camera().getEulerAngles();
		kfJson.addProperty("yaw", Math.toDegrees(Math.PI + eulerAngle.y));
		kfJson.addProperty("pitch", Math.toDegrees(eulerAngle.x));
		kfJson.addProperty("roll", Math.toDegrees(eulerAngle.z));
		kfJson.addProperty("type", "camera");
		kfJson.addProperty("interpolation_type", "SMOOTH");
		return kfJson;
	}
}
