package lpc.javaTools.media.audio;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lpc.javaTools.media.FFmpegUtils;
import lpc.javaTools.utils.math.MathUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class FFmpegAudioUtils extends FFmpegUtils {
	// =========================
	// 解码
	// =========================
	public static PCMData decodeToPCM(File file) throws IOException {
		if(logInfo) logger.info("Start decoding file to PCM");
		
		File temp = File.createTempFile("audio_decode", ".raw");
		
		// ffmpeg -> raw PCM (使用浮点格式，让FFmpeg处理转换)
		runFFmpeg(List.of(
			"ffmpeg",
			"-y",
			"-i", file.getAbsolutePath(),
			"-f", "f32le", // 32bit float little-endian
			"-acodec", "pcm_f32le",
			"-"
		), temp);
		
		// 获取信息（简单方式：再跑一次 ffprobe 也可以）
		AudioInfo info = probe(file);
		
		byte[] raw = readAllBytes(temp);
		
		int totalSamples = raw.length / 4 / info.channels();
		
		float[][] buffer = new float[info.channels()][totalSamples];
		
		ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
		
		for (int i = 0; i < totalSamples; i++) {
			for (int ch = 0; ch < info.channels(); ch++) {
				buffer[ch][i] = bb.getFloat();
			}
		}
		
		PCMData data = new PCMData(buffer, info.sampleRate());
		
		deleteTempFile(temp);
		
		if(logInfo) logger.info("Completed decoding file to PCM");
		
		return data;
	}
	
	public static PCMData decodeToPCM(String path) throws IOException {
		return decodeToPCM(new File(path));
	}
	
	// =========================
	// 编码（无损优先）
	// =========================
	public static void encodeFromPCM(
		PCMData data, File output,
		boolean autoDecreaseVolume, boolean autoIncreaseVolume
	) throws IOException {
		if(logInfo) logger.info("Start encoding file from PCM");
		
		EncodingParams params = getEncodingParams(output);
		
		File temp = File.createTempFile("audio_encode", ".raw");
		
		writePCM(data.samples, temp, autoDecreaseVolume, autoIncreaseVolume);
		
		List<String> cmd = new ArrayList<>();
		cmd.add("ffmpeg");
		cmd.add("-y");
		cmd.add("-f");
		cmd.add("f32le");
		cmd.add("-ar");
		cmd.add(String.valueOf(data.sampleRate));
		cmd.add("-ac");
		cmd.add(String.valueOf(data.samples.length));
		cmd.add("-i");
		cmd.add(temp.getAbsolutePath());
		cmd.add("-c:a");
		cmd.add(params.codec);
		if(params.sampleFormat != null) {
			cmd.add("-sample_fmt");
			cmd.add(params.sampleFormat);
		}
		if (params.strictExperimental) {
			cmd.add("-strict");
			cmd.add("experimental");
		}
		cmd.add(output.getAbsolutePath());
		
		runFFmpeg(cmd, null);
		
		deleteTempFile(temp);
		
		if(logInfo) logger.info("Completed encoding file from PCM");
	}
	public static void encodeFromPCM(
		PCMData data, String filePath,
		boolean autoDecreaseVolume, boolean autoIncreaseVolume
	) throws IOException {
		encodeFromPCM(data, new File(filePath), autoDecreaseVolume, autoIncreaseVolume);
	}
	public static void encodeFromPCM(
		PCMData data, File output
	) throws IOException {
		encodeFromPCM(data, output, false, false);
	}
	public static void encodeFromPCM(
		PCMData data, String filePath
	) throws IOException {
		encodeFromPCM(data, new File(filePath), false, false);
	}
	
	private static EncodingParams getEncodingParams(File output) {
		String name = output.getName().toLowerCase();
		EncodingParams params = new EncodingParams();
		if (name.endsWith(".flac")) {
			params.codec = "flac";
			params.sampleFormat = "s16";
			params.strictExperimental = true;
		} else if (name.endsWith(".wav")) {
			params.codec = "pcm_s16le";
			params.strictExperimental = false;
		} else if (name.endsWith(".mp3")) {
			params.codec = "libmp3lame";
			params.strictExperimental = false;
		} else if (name.endsWith(".aac")) {
			params.codec = "aac";
			params.strictExperimental = false;
		} else if (name.endsWith(".ogg")) {
			params.codec = "libvorbis";
			params.strictExperimental = false;
		} else if (name.endsWith(".m4a")) {
			// M4A格式 (MP4容器中的AAC)
			params.codec = "aac";
			params.strictExperimental = false;
		} else {
			params.codec = "flac";
			params.sampleFormat = "s16";
			params.strictExperimental = true;
		}
		return params;
	}
	
	private static class EncodingParams {
		String codec;
		String sampleFormat = null;
		boolean strictExperimental;
	}
	
	// =========================
	// 内部工具
	// =========================

	private static void writePCM(float[][] buffer, File file, boolean autoDecreaseVolume, boolean autoIncreaseVolume) throws IOException {
		int samples = buffer[0].length;
		int channels = buffer.length;
		ByteBuffer bb = ByteBuffer.allocate(samples * channels * 4).order(ByteOrder.LITTLE_ENDIAN);
		
		float k;
		if(autoIncreaseVolume || autoDecreaseVolume) {
			float max = MathUtils.floatMax(MathUtils.toFloatApply(MathUtils::floatMax, buffer));
			if(max < 1.0f ? autoIncreaseVolume : autoDecreaseVolume) k = 1.0f / max;
			else k = 1.0f;
		}
		else k = 1.0f;
		
		if(k == 1.0f) {
			for (int i = 0; i < samples; i++)
				for (float[] floats : buffer)
					bb.putFloat(floats[i]);
		}
		else {
			if(logInfo) logger.info("Volume too {}, adjusting by multiplying factor {}", k < 1.0f ? "big" : "small",  k);
			for (int i = 0; i < samples; i++)
				for (float[] floats : buffer)
					bb.putFloat(floats[i] * k);
		}
		
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(bb.array());
		}
	}
	
	static class FFProbeRaw {
		static class Result {
			List<Stream> streams;
		}
		
		static class Stream{
			@SerializedName("codec_type")
			String codecType;
			
			Integer channels;
			
			@SerializedName("sample_rate")
			String sampleRate;
			
			@SerializedName("bits_per_sample")
			Integer bitsPerSample;
			
			@SerializedName("sample_fmt")
			String sampleFmt;
		}
	}
	
	public static AudioInfo probe(File file) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(
				"ffprobe",
				"-v", "quiet",
				"-print_format", "json",
				"-show_streams",
				file.getAbsolutePath()
			);
			
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);
			
			Process p = pb.start();
			
			String json;
			try (InputStream in = p.getInputStream()) {
				json = new String(in.readAllBytes());
			}
			
			int code = p.waitFor();
			if (code != 0) {
				throw new IOException("ffprobe failed with code " + code);
			}
			
			// ✅ Gson 自动解析
			Gson gson = new Gson();
			FFProbeRaw.Result result = gson.fromJson(json, FFProbeRaw.Result.class);
			
			if (result.streams == null) {
				throw new IOException("No streams found");
			}
			
			// ✅ 找 audio stream
			FFProbeRaw.Stream audio = null;
			for (FFProbeRaw.Stream s : result.streams) {
				if ("audio".equals(s.codecType)) {
					audio = s;
					break;
				}
			}
			
			if (audio == null) {
				throw new IOException("No audio stream found");
			}
			
			// ✅ 类型转换 + 默认值处理
			int channels = audio.channels != null ? audio.channels : 2;
			
			int sampleRate = audio.sampleRate != null
				? Integer.parseInt(audio.sampleRate)
				: 44100;
			
			int bits = audio.bitsPerSample != null
				? audio.bitsPerSample
				: 16;
			
			String fmt = audio.sampleFmt != null
				? audio.sampleFmt
				: "s16";
			
			return new AudioInfo(channels, sampleRate, bits, fmt);
			
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
}
