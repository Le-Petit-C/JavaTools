package lpc.javaTools.media.audio;

public class PCMData implements Cloneable {
	public float[][] samples;
	public int sampleRate;
	
	public PCMData(float[][] samples, int sampleRate) {
		this.samples = samples;
		this.sampleRate = sampleRate;
	}
	
	@Override
	public PCMData clone() {
		try {
			PCMData res = (PCMData) super.clone();
			res.samples = new float[this.samples.length][];
			for (int i = 0; i < this.samples.length; i++)
				res.samples[i] = this.samples[i].clone();
			return res;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported", e);
		}
	}
}
