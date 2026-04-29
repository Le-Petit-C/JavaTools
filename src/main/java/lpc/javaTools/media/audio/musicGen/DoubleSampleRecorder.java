package lpc.javaTools.media.audio.musicGen;

import lpc.javaTools.utils.math.MathUtils;

import java.util.Arrays;

public class DoubleSampleRecorder {
	private double[] samples = new double[256];
	private int sampleCount = 0;
	public void superpose(int index, double value) {
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
		double[] newSamples = new double[Math.max(sampleCount, samples.length * 2)];
		System.arraycopy(samples, 0, newSamples, 0, samples.length);
		samples = newSamples;
		samples[index] += value;
	}
	public double[] trimAndGet() {
		if(sampleCount != samples.length) {
			double[] newSamples = new double[sampleCount];
			System.arraycopy(samples, 0, newSamples, 0, sampleCount);
			samples = newSamples;
		}
		return samples;
	}
	public int getSampleCount() {
		return sampleCount;
	}
	public double[] getSamples(double[] samples) {
		int count = Math.min(sampleCount, samples.length);
		System.arraycopy(this.samples, 0, samples, 0, count);
		if(count < samples.length) Arrays.fill(samples, count, samples.length, 0);
		return samples;
	}
	public int[] getMaxSamples(int[] samples) {
		double k = (double)Integer.MAX_VALUE / MathUtils.max(this.samples, Math::abs);
		int count = Math.min(sampleCount, samples.length);
		for (int i = 0; i < count; ++i)
			samples[i] = (int)Math.round(k * this.samples[i]);
		if(count < samples.length) Arrays.fill(samples, count, samples.length, 0);
		return samples;
	}
}
