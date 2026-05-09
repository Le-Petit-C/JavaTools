package lpc.javaTools.media.audio.musicGen;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class ToneFrequencies {
	public static double A4 = 440;
	public static float A4_F = (float) A4;
	private static final Int2DoubleOpenHashMap recordedValues = new Int2DoubleOpenHashMap();
	public static double shiftedFrequency(int section, int subSection) {
		int v = section * 12 + subSection;
		if (recordedValues.containsKey(v)) return recordedValues.get(v);
		else {
			double value = A4 * Math.pow(2, v / 12.0);
			recordedValues.put(v, value);
			return value;
		}
	}
}
