package lpc.javaTools.media.audio;

public record AudioInfo(
	int channels,
	int sampleRate,
	int bitsPerSample,
	String sampleFormat
) {}
