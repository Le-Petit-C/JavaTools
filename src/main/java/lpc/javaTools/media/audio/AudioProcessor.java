package lpc.javaTools.media.audio;

import lpc.javaTools.media.FFmpegUtils;

import java.io.File;
import java.io.IOException;

public class AudioProcessor implements IAudioProcessor {
	FFmpegUtils.PCMData data;  // 原始数据（用于比较）
	
	public AudioProcessor(File file) throws IOException {
		// 使用 FFmpeg 解码为 PCM
		data = FFmpegUtils.decodeToPCM(file);
	}
	
	@Override public FFmpegUtils.PCMData getPCMData() { return data; }
	@Override public void save(File file) throws IOException {
		FFmpegUtils.encodeFromPCM(
			data.samples(),
			data.audioInfo().sampleRate(),
			data.audioInfo().bitsPerSample(),
			file
		);
	}
}