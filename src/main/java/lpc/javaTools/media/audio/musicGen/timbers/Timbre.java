package lpc.javaTools.media.audio.musicGen.timbers;

import lpc.javaTools.media.audio.musicGen.SampleRecorder;

public interface Timbre {
	void superpose(SampleRecorder samples, double startTime, double duration, int sampleRate, double frequency, float volume);
}
