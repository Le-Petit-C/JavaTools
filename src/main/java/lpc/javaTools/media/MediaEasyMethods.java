package lpc.javaTools.media;

import lpc.javaTools.media.audio.FFmpegAudioUtils;
import lpc.javaTools.media.audio.PCMData;
import lpc.javaTools.utils.FileUtils;
import lpc.javaTools.utils.math.MathHelper;
import lpc.javaTools.utils.math.MathUtils;

import java.io.File;
import java.io.IOException;

public class MediaEasyMethods {
	public static void balanceChannels(File input, File output, boolean balanceOffset) throws IOException {
		PCMData pcmData = FFmpegAudioUtils.decodeToPCM(input);
		if(balanceOffset) {
			float[] rawLeft = pcmData.samples[0];
			float[] rawRight = pcmData.samples[1];
			float[] relative = MathUtils.fastConvolution(rawLeft, MathUtils.selfInverse(rawRight));
			int rightOffset = MathUtils.maxIndexOf(relative) - rawLeft.length + 1;
			if(rightOffset != 0) {
				float[] newLeft = new float[rawLeft.length + Math.abs(rightOffset)];
				float[] newRight = new float[newLeft.length];
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
		float[][] buffer = pcmData.samples;
		float[] channelStrength = new float[buffer.length];
		for(int i = 0; i < buffer.length; ++i) {
			float last = 0;
			float res = 0;
			for(float sample : buffer[i]) {
				res += (sample - last) * (sample - last);
				last = sample;
			}
			channelStrength[i] = MathHelper.sqrt(res / buffer[i].length);
		}
		float minStrength = MathUtils.min(channelStrength);
		for (int ch = 0; ch < channelStrength.length; ++ch) {
			float strength = channelStrength[ch];
			if(strength != minStrength) {
				float k = minStrength / strength;
				System.out.println("k: " + k);
				MathUtils.selfApply(x -> x * k, buffer[ch]);
			}
		}
		FFmpegAudioUtils.encodeFromPCM(pcmData, output);
	}
	public static void balanceChannels(String path, boolean balanceOffset) throws IOException {
		File outputFile = FileUtils.nextNotExistWithSuffix(path, "_balanced");
		MediaEasyMethods.balanceChannels(new File(path), outputFile, balanceOffset);
	}
}
