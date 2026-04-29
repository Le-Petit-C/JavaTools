package lpc.javaTools.media.audio;

public class PCMData {
	public int[][] samples;
	public int sampleRate;
	
	public PCMData(int[][] samples, int sampleRate) {
		this.samples = samples;
		this.sampleRate = sampleRate;
	}
}
