package lpc.javaTools.media.audio.musicGen.timbers;

import lpc.javaTools.media.audio.musicGen.SampleRecorder;
import lpc.javaTools.minecraft.replay.Ease;
import lpc.javaTools.utils.math.eases.LinearEase;

public class SinWave implements Timbre {
	Ease easeIn, easeOut;
	double easeInPeriods, easeOutPeriods;
	public SinWave(Ease easeIn, Ease easeOut, double easeInPeriods, double easeOutPeriods) {
		this.easeIn = easeIn;
		this.easeOut = easeOut;
		this.easeInPeriods = easeInPeriods;
		this.easeOutPeriods = easeOutPeriods;
	}
	public SinWave() {
		this(LinearEase.instance, LinearEase.instance, 10, 10);
	}
	@Override public void superpose(SampleRecorder samples, double startTime, double duration, int sampleRate, double frequency, float volume) {
		double startSample = startTime * sampleRate;
		double samplePerPeriod = sampleRate / frequency;
		double easeInEndSample = startSample + samplePerPeriod * easeInPeriods;
		double endSample = startSample + duration * sampleRate;
		if(easeInEndSample > endSample) throw new IllegalArgumentException("easeInEndSample > endSample");
		double easeOutEndSample = endSample + samplePerPeriod * easeOutPeriods;
		int iStartSample = (int) Math.ceil(startSample);
		double phasePerSample = Math.PI * 2 / samplePerPeriod;
		double startPhase = (iStartSample - startSample) * phasePerSample;
		double baseSin = Math.sin(phasePerSample), baseCos = Math.cos(phasePerSample);
		double sin = Math.sin(startPhase), cos = Math.cos(startPhase);
		int iEaseInEndSample = (int) Math.floor(easeInEndSample);
		for(int i = iStartSample; i <= iEaseInEndSample; ++i) {
			float k = volume * (float)easeIn.translate((i - startSample) / (easeInEndSample - startSample));
			samples.superpose(i, (float)(k * sin));
			double newSin = sin * baseCos + baseSin * cos;
			double newCos = cos * baseCos - sin * baseSin;
			double k1 = 1.0 / Math.sqrt(newSin * newSin + newCos * newCos);
			sin = newSin * k1;
			cos = newCos * k1;
		}
		int iEndSample = (int) Math.floor(endSample);
		for(int i = iEaseInEndSample + 1; i <= iEndSample; ++i) {
			samples.superpose(i, (float)(volume * sin));
			double newSin = sin * baseCos + baseSin * cos;
			double newCos = cos * baseCos - sin * baseSin;
			double k1 = 1.0 / Math.sqrt(newSin * newSin + newCos * newCos);
			sin = newSin * k1;
			cos = newCos * k1;
		}
		int iEaseOutEndSample = (int) Math.floor(easeOutEndSample);
		for(int i = iEndSample + 1; i <= iEaseOutEndSample; ++i) {
			float k = volume * (float)easeOut.translate((easeOutEndSample - i) / (easeOutEndSample - endSample));
			samples.superpose(i, (float)(k * sin));
			double newSin = sin * baseCos + baseSin * cos;
			double newCos = cos * baseCos - sin * baseSin;
			double k1 = 1.0 / Math.sqrt(newSin * newSin + newCos * newCos);
			sin = newSin * k1;
			cos = newCos * k1;
		}
	}
}
