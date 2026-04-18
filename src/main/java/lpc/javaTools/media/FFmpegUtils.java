package lpc.javaTools.media;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class FFmpegUtils {
	
	public record PCMData(int[][] samples, AudioInfo audioInfo) {}
	
	public record AudioInfo(
		int channels,
		int sampleRate,
		int bitsPerSample,
		String sampleFormat
	) {}
	
	// =========================
	// 解码
	// =========================
	public static PCMData decodeToPCM(File file) throws IOException {
		
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
		
		int totalSamples = raw.length / 4 / info.channels;
		
		int[][] buffer = new int[info.channels][totalSamples];
		
		ByteBuffer bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
		
		for (int i = 0; i < totalSamples; i++) {
			for (int ch = 0; ch < info.channels; ch++) {
				buffer[ch][i] = bb.getInt();
			}
		}
		
		PCMData data = new PCMData(buffer, info);
		
		deleteTempFile(temp);
		
		return data;
	}
	
	// =========================
	// 编码（无损优先）
	// =========================
	public static void encodeFromPCM(
		int[][] buffer,
		double sampleRate,
		int bits,
		File output
	) throws IOException {
		
		File temp = File.createTempFile("audio_encode", ".raw");
		
		writePCM(buffer, temp);
		
		String codec = getCodecForExtension(output);
		
		runFFmpeg(List.of(
			"ffmpeg",
			"-y",
			"-f", "s32le",
			"-ar", String.valueOf((int) sampleRate),
			"-ac", String.valueOf(buffer.length),
			"-i", temp.getAbsolutePath(),
			"-c:a", codec,
			output.getAbsolutePath()
		), null);
		
		deleteTempFile(temp);
	}
	
	private static String getCodecForExtension(File output) {
		String name = output.getName().toLowerCase();
		if (name.endsWith(".flac")) return "flac";
		if (name.endsWith(".wav")) return "pcm_s16le"; // or pcm_s32le if bits==32
		if (name.endsWith(".mp3")) return "libmp3lame";
		if (name.endsWith(".aac")) return "aac";
		if (name.endsWith(".ogg")) return "libvorbis";
		// default to flac for lossless
		return "flac";
	}
	
	// =========================
	// 直接复制（无损）
	// =========================
	public static void streamCopy(File input, File output) throws IOException {
		runFFmpeg(List.of(
			"ffmpeg",
			"-i", input.getAbsolutePath(),
			"-c", "copy",
			output.getAbsolutePath()
		), null);
	}
	
	// =========================
	// 内部工具
	// =========================
	
	private static void writePCM(int[][] buffer, File file) throws IOException {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			int samples = buffer[0].length;
			
			for (int i = 0; i < samples; i++) {
				for (int[] ints : buffer) {
					out.writeInt(Integer.reverseBytes(ints[i]));
				}
			}
		}
	}
	
	private static void runFFmpeg(List<String> cmd, File outputFile) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		
		if (outputFile != null) {
			pb.redirectOutput(outputFile);
		} else {
			pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		}
		
		Process p;
		try {
			p = pb.start();
			int code = p.waitFor();
			if (code != 0) {
				throw new IOException("ffmpeg failed with code " + code);
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	private static byte[] readAllBytes(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return in.readAllBytes();
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
	
	private static void deleteTempFile(File file) {
		if(!file.delete()) LogManager.getLogger().warn("Failed to delete temp file: {}", file.getAbsolutePath());
	}
}

