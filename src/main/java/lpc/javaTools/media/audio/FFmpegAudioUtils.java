package lpc.javaTools.media.audio;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lpc.javaTools.media.FFmpegUtils;
import org.apache.logging.log4j.LogManager;

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
		if(logInfo) LogManager.getLogger().info("Start decoding file to PCM");
		
		File temp = File.createTempFile("audio_decode", ".raw");
		
		// ffmpeg -> raw PCM
		runFFmpeg(List.of(
			"ffmpeg",
			"-i", file.getAbsolutePath(),
			"-f", "s32le", // 统一用 32bit PCM
			"-acodec", "pcm_s32le",
			"-"
		), temp);
		
		// 获取信息（简单方式：再跑一次 ffprobe 也可以）
		AudioInfo info = probe(file);
		
		byte[] raw = readAllBytes(temp);
		
		int totalSamples = raw.length / 4 / info.channels();
		
		int[][] buffer = new int[info.channels()][totalSamples];
		
		ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
		
		for (int i = 0; i < totalSamples; i++) {
			for (int ch = 0; ch < info.channels(); ch++) {
				buffer[ch][i] = bb.getInt();
			}
		}
		
		PCMData data = new PCMData(buffer, info.sampleRate());
		
		deleteTempFile(temp);
		
		if(logInfo) LogManager.getLogger().info("Completed decoding file to PCM");
		
		return data;
	}
	
	// =========================
	// 编码（无损优先）
	// =========================
	public static void encodeFromPCM(
		PCMData data,
		File output
	) throws IOException {
		if(logInfo) LogManager.getLogger().info("Start encoding file from PCM");
		
		EncodingParams params = getEncodingParams(output);
		
		File temp = File.createTempFile("audio_encode", ".raw");
		
		writePCM(data.samples, temp, params.bits);
		
		List<String> cmd = new ArrayList<>();
		cmd.add("ffmpeg");
		cmd.add("-y");
		cmd.add("-f");
		cmd.add(params.format);
		cmd.add("-ar");
		cmd.add(String.valueOf(data.sampleRate));
		cmd.add("-ac");
		cmd.add(String.valueOf(data.samples.length));
		cmd.add("-i");
		cmd.add(temp.getAbsolutePath());
		cmd.add("-c:a");
		cmd.add(params.codec);
		if (params.strictExperimental) {
			cmd.add("-strict");
			cmd.add("experimental");
		}
		cmd.add(output.getAbsolutePath());
		
		runFFmpeg(cmd, null);
		
		deleteTempFile(temp);
		
		if(logInfo) LogManager.getLogger().info("Completed encoding file from PCM");
	}
	public static void encodeFromPCM(
		PCMData data,
		String filePath
	) throws IOException {
		encodeFromPCM(data, new File(filePath));
	}
	
	private static EncodingParams getEncodingParams(File output) {
		String name = output.getName().toLowerCase();
		EncodingParams params = new EncodingParams();
		if (name.endsWith(".flac")) {
			params.format = "s32le";
			params.codec = "flac";
			params.strictExperimental = true;
			params.bits = 32;
		} else if (name.endsWith(".wav")) {
			params.format = "s16le";
			params.codec = "pcm_s16le";
			params.strictExperimental = false;
			params.bits = 16;
		} else if (name.endsWith(".mp3")) {
			params.format = "s16le";
			params.codec = "libmp3lame";
			params.strictExperimental = false;
			params.bits = 16;
		} else if (name.endsWith(".aac")) {
			params.format = "s16le";
			params.codec = "aac";
			params.strictExperimental = false;
			params.bits = 16;
		} else if (name.endsWith(".ogg")) {
			params.format = "s16le";
			params.codec = "libvorbis";
			params.strictExperimental = false;
			params.bits = 16;
		} else {
			params.format = "s32le";
			params.codec = "flac";
			params.strictExperimental = true;
			params.bits = 32;
		}
		return params;
	}
	
	private static class EncodingParams {
		String format;
		String codec;
		boolean strictExperimental;
		int bits;
	}
	
	// =========================
	// 内部工具
	// =========================
	
	private static void writePCM(int[][] buffer, File file) throws IOException {
		writePCM(buffer, file, 16);
	}
	private static void writePCM(int[][] buffer, File file, int bitsPerSample) throws IOException {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			int samples = buffer[0].length;
			
			for (int i = 0; i < samples; i++) {
				for (int[] ints : buffer) {
					if (bitsPerSample == 16) {
						out.writeShort(Short.reverseBytes((short)ints[i]));
					} else if (bitsPerSample == 24) {
						out.writeInt(Integer.reverseBytes(ints[i] << 8));
					} else {
						out.writeInt(Integer.reverseBytes(ints[i]));
					}
				}
			}
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
