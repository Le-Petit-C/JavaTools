package lpc.javaTools.media.audio;

import lpc.javaTools.media.FFmpegUtils;

public class PCMData {
	public int[][] samples;
	public FFmpegUtils.AudioInfo audioInfo;
	
	public PCMData(int[][] samples, FFmpegUtils.AudioInfo audioInfo) {
		this.samples = samples;
		this.audioInfo = audioInfo;
	}
	
	
}
