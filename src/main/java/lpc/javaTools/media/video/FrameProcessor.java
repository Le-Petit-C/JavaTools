package lpc.javaTools.media.video;

import lpc.javaTools.media.image.ImageData;

public interface FrameProcessor {
	void processNextFrame(int frameIndex, ImageData frame);
}
