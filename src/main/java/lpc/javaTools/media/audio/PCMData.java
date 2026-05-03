package lpc.javaTools.media.audio;

public class PCMData {
	public float[][] samples;
	public int sampleRate;
	
	public PCMData(float[][] samples, int sampleRate) {
		this.samples = samples;
		this.sampleRate = sampleRate;
	}
}
