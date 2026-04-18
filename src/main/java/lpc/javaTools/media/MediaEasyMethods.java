package lpc.javaTools.media;

import lpc.javaTools.media.audio.AudioProcessor;
import lpc.javaTools.utils.MathUtils;

import java.io.File;
import java.io.IOException;

public class MediaEasyMethods {
	public static void balanceChannels(File input, File output) throws IOException {
		AudioProcessor processor = new AudioProcessor(input);
		int[][] buffer = processor.getBuffer();
		double[] channelStrength = MathUtils.apply(channel -> Math.sqrt(MathUtils.average(MathUtils.apply(x -> (double)x * x, channel))), buffer);
		double minStrength = MathUtils.min(channelStrength);
		for (int ch = 0; ch < channelStrength.length; ++ch) {
			double strength = channelStrength[ch];
			if(strength != minStrength) {
				float k = (float) (minStrength / strength);
				MathUtils.selfApply(x -> Math.round(x * k), buffer[ch]);
			}
		}
		processor.save(output);
	}
	public static void balanceChannels(String path) throws IOException {
		int dotIndex = path.lastIndexOf('.');
		String outputPath;
		if (dotIndex == -1) outputPath = path + "_balanced";
		else outputPath = path.substring(0, dotIndex) + "_balanced" + path.substring(dotIndex);
		MediaEasyMethods.balanceChannels(new File(path), new File(outputPath));
	}
}
