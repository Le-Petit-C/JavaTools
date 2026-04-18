package lpc.javaTools.media;

import lpc.javaTools.media.audio.PCMData;
import lpc.javaTools.utils.MathUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MediaEasyMethods {
	public static void balanceChannels(File input, File output, boolean balanceOffset) throws IOException {
		PCMData pcmData = FFmpegUtils.decodeToPCM(input);
		if(balanceOffset) {
			int[] rawLeft = pcmData.samples[0];
			int[] rawRight = pcmData.samples[1];
			double[] relative = MathUtils.fastConvolution(MathUtils.apply(v->v, rawLeft), MathUtils.selfInverse(MathUtils.apply(v->v, rawRight)));
			int rightOffset = MathUtils.maxIndexOf(relative) - rawLeft.length + 1;
			if(rightOffset != 0) {
				int[] newLeft = new int[rawLeft.length + Math.abs(rightOffset)];
				int[] newRight = new int[newLeft.length];
				if(rightOffset > 0) {
					System.arraycopy(rawLeft, 0, newLeft, 0, rawLeft.length);
					System.arraycopy(rawRight, 0, newRight, rightOffset, rawRight.length);
				} else {
					System.arraycopy(rawLeft, 0, newLeft, -rightOffset, rawLeft.length);
					System.arraycopy(rawRight, 0, newRight, 0, rawRight.length);
				}
				pcmData.samples[0] = newLeft;
				pcmData.samples[1] = newRight;
			}
		}
		int[][] buffer = pcmData.samples;
		double[] channelStrength = new double[buffer.length];
		for(int i = 0; i < buffer.length; ++i) {
			int last = 0;
			double res = 0;
			for(int sample : buffer[i]) {
				res += (double)(sample - last) * (sample - last);
				last = sample;
			}
			channelStrength[i] = Math.sqrt(res / buffer[i].length);
		}
		/* = MathUtils.apply(
			channel ->
				Math.sqrt(MathUtils.average(x -> (double)x * x, channel) -
					MathUtils.square(MathUtils.average(x -> (double)x, channel))
				), buffer
		);*/
		double minStrength = MathUtils.min(channelStrength);
		for (int ch = 0; ch < channelStrength.length; ++ch) {
			double strength = channelStrength[ch];
			if(strength != minStrength) {
				float k = (float) (minStrength / strength);
				System.out.println("k: " + k);
				MathUtils.selfApply(x -> Math.round(x * k), buffer[ch]);
			}
		}
		FFmpegUtils.encodeFromPCM(pcmData, output);
	}
	public static void balanceChannels(String path, boolean balanceOffset) throws IOException {
		int dotIndex = path.lastIndexOf('.');
		String outputPath;
		if (dotIndex == -1) outputPath = path + "_balanced";
		else outputPath = path.substring(0, dotIndex) + "_balanced" + path.substring(dotIndex);
		MediaEasyMethods.balanceChannels(new File(path), new File(outputPath), balanceOffset);
	}
}
