package lpc.javaTools.media.audio.musicGen;

import java.util.Arrays;

public class SampleRecorder {
	private float[] samples = new float[256];
	private int sampleCount = 0;
	public void superpose(int index, float value) {
		// 优先走短路径，提高运行效率
		if(index < sampleCount) {
			samples[index] += value;
			return;
		}
		sampleCount = index + 1;
		if(sampleCount < samples.length) {
			samples[index] += value;
			return;
		}
		float[] newSamples = new float[Math.max(sampleCount, samples.length * 2)];
		System.arraycopy(samples, 0, newSamples, 0, samples.length);
		samples = newSamples;
		samples[index] += value;
	}
	public float[] trimAndGet() {
		if(sampleCount != samples.length) {
			float[] newSamples = new float[sampleCount];
			System.arraycopy(samples, 0, newSamples, 0, sampleCount);
			samples = newSamples;
		}
		return samples;
	}
	public int getSampleCount() {
		return sampleCount;
	}
	public float[] getSamples(float[] samples) {
		int count = Math.min(sampleCount, samples.length);
		System.arraycopy(this.samples, 0, samples, 0, count);
		if(count < samples.length) Arrays.fill(samples, count, samples.length, 0);
		return samples;
	}
}
