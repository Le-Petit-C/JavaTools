package lpc.javaTools.media.video;

import lpc.javaTools.media.audio.PCMData;
import lpc.javaTools.media.image.ImageData;
import lpc.javaTools.utils.memory.QuietAutoCloseable;

public interface VideoEncoder extends QuietAutoCloseable {
	void setFrame(int frameIndex, ImageData frame);
	@Override void close();
	void closeWithAudio(PCMData audioData, boolean cutIfAudioTooLong);
}
