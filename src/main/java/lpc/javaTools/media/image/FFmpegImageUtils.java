package lpc.javaTools.media.image;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lpc.javaTools.media.FFmpegUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class FFmpegImageUtils extends FFmpegUtils {
	
	// =========================
	// 图片解析和保存
	// =========================
	public static ImageInfo probeImage(Path path) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder(
				"ffprobe",
				"-v", "quiet",
				"-print_format", "json",
				"-show_streams",
				path.toAbsolutePath().toString()
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
			
			Gson gson = new Gson();
			FFProbeRaw.Result result = gson.fromJson(json, FFProbeRaw.Result.class);
			
			if (result.streams == null) {
				throw new IOException("No streams found");
			}
			
			FFProbeRaw.Stream video = null;
			for (FFProbeRaw.Stream s : result.streams) {
				if ("video".equals(s.codecType)) {
					video = s;
					break;
				}
			}
			
			if (video == null) {
				throw new IOException("No video stream found");
			}
			
			int width = video.width != null ? video.width : 0;
			int height = video.height != null ? video.height : 0;
			String pixFmt = video.pixFmt != null ? video.pixFmt : "rgb24";
			
			return new ImageInfo(width, height, pixFmt);
			
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	public static ImageData decodeImage(Path path) throws IOException {
		if(logInfo) logger.info("Start decoding image file");
		
		ImageInfo info = probeImage(path);
		
		Path temp = File.createTempFile("image_decode", ".raw").toPath();
		
		runFFmpeg(List.of(
			"ffmpeg",
			"-y",
			"-i", path.toAbsolutePath().toString(),
			"-f", "rawvideo",
			"-pix_fmt", "rgba",
			temp.toAbsolutePath().toString()
		), null);
		
		byte[] data = readAllBytes(temp);
		
		deleteTempFile(temp);
		
		ImageData imageData = new ImageData(info.width(), info.height());
		float[] rawFloats = imageData.getRawFloatArray();
		int i = 0;
		for (byte b : data) rawFloats[i++] = Byte.toUnsignedInt(b) / 255.0f;
		
		if(logInfo) logger.info("Completed decoding image file");
		
		return imageData;
	}
	
	public static ImageData decodeImage(String filePath) throws IOException {
		return decodeImage(Path.of(filePath));
	}
	
	public static void encodeImage(byte[] pixelData, int width, int height, String pixFmt, Path output) throws IOException {
		if(logInfo) logger.info("Start encoding image file");
		
		Path temp = File.createTempFile("image_encode", ".raw").toPath();
		
		try (FileOutputStream fos = new FileOutputStream(temp.toFile())) {
			fos.write(pixelData);
		}
		
		runFFmpeg(List.of(
			"ffmpeg",
			"-y",
			"-f", "rawvideo",
			"-pix_fmt", pixFmt,
			"-s", width + "x" + height,
			"-i", temp.toAbsolutePath().toString(),
			output.toAbsolutePath().toString()
		), null);
		
		deleteTempFile(temp);
		
		if(logInfo) logger.info("Completed encoding image file");
	}
	
	public static void encodeImage(ImageData data, Path output) throws IOException {
		int width = data.getWidth();
		int height = data.getHeight();
		byte[] pixelData = new byte[width * height * 4];
		float[] rawFloats = data.getRawFloatArray();
		int i = 0;
		for(float b : rawFloats) pixelData[i++] = (byte) Math.round(b * 255);
		encodeImage(pixelData, width, height, "rgba", output);
	}
	
	public static void encodeImage(ImageData data, String filePath) throws IOException {
		encodeImage(data, Path.of(filePath));
	}
	
	// =========================
	// 内部工具
	// =========================
	
	static class FFProbeRaw {
		static class Result {
			List<Stream> streams;
		}
		
		static class Stream{
			@SerializedName("codec_type")
			String codecType;
			
			@SerializedName("width")
			Integer width;
			
			@SerializedName("height")
			Integer height;
			
			@SerializedName("pix_fmt")
			String pixFmt;
		}
	}
}
