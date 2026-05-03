package lpc.javaTools.media;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FFmpegUtils {
	public static boolean logInfo = false;
	
	protected static final Logger logger = LogManager.getLogger("FFmpegUtils");
	
	protected static void deleteTempFile(File file) {
		if(!file.delete()) LogManager.getLogger().warn("Failed to delete temp file: {}", file.getAbsolutePath());
	}
	
	protected static void runFFmpeg(List<String> cmd, File outputFile) throws IOException {
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
	
	protected static byte[] readAllBytes(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return in.readAllBytes();
		}
	}
}
