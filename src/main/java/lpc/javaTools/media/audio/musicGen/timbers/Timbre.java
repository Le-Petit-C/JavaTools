package lpc.javaTools.media.audio.musicGen.timbers;

import lpc.javaTools.media.audio.musicGen.DoubleSampleRecorder;

public interface Timbre {
	void superpose(DoubleSampleRecorder samples, double startTime, double duration, double sampleRate, double frequency, double volume);
}
