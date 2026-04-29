package lpc.javaTools.minecraft.replay;

import lpc.javaTools.utils.math.eases.LinearEase;
import lpc.javaTools.utils.ClipboardUtils;

import java.util.ArrayList;

public class GenerateController {
	private final CameraGen cameraGen;
	private final Output output;
	private double gameTime = 10, videoTime = 10;
	private int intervalCount = 1;
	private Ease gameTimeEase = LinearEase.instance;
	private Ease videoTimeEase = LinearEase.instance;
	private boolean skipHead = false, skipTail = false;
	public GenerateController(CameraGen cameraGen, Output output) {
		this.cameraGen = cameraGen;
		this.output = output;
	}
	
	public void preGenerate(double gameTime, double videoTime, int intervalCount, Ease gameTimeEase, Ease videoTimeEase) {
		this.gameTime = gameTime;
		this.videoTime = videoTime;
		this.intervalCount = intervalCount;
		this.gameTimeEase = gameTimeEase;
		this.videoTimeEase = videoTimeEase;
	}
	
	public void preGenerate(double gameTime, double videoTime, int intervalCount) {
		preGenerate(gameTime, videoTime, intervalCount, LinearEase.instance, LinearEase.instance);
	}
	
	public String generate() {
		ArrayList<KeyFrame> keyFrames = new ArrayList<>();
		int start = skipHead ? 1 : 0;
		int end = skipTail ? intervalCount - 1 : intervalCount;
		for(int i = start; i <= end; ++i) {
			double rawTime = (double)i / intervalCount;
			double gameTime = gameTimeEase.translate(rawTime) * this.gameTime;
			double videoTime = videoTimeEase.translate(rawTime) * this.videoTime;
			keyFrames.add(new KeyFrame(cameraGen.generate(new Camera(), rawTime), gameTime, videoTime));
		}
		return output.generate(keyFrames);
	}
	
	public void generateToClipBoard() {
		ClipboardUtils.copyToClipboard(generate());
	}
	
	public void skipHead(boolean skipHead) {this.skipHead = skipHead;}
	public void skipTail(boolean skipTail) {this.skipTail = skipTail;}
	public void skipHead() {skipHead(true);}
	public void skipTail() {skipTail(true);}
}
