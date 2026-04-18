package lpc.javaTools.media.audio;

import lpc.javaTools.media.FFmpegUtils;

import java.io.File;
import java.io.IOException;

public interface IAudioProcessor {
	FFmpegUtils.PCMData getPCMData();
	default int[][] getBuffer() { return getPCMData().samples(); }
	default int bitsPerSample() { return getPCMData().audioInfo().bitsPerSample(); }
	default double sampleRate() { return getPCMData().audioInfo().sampleRate(); }
	void save(File file) throws IOException;
}
