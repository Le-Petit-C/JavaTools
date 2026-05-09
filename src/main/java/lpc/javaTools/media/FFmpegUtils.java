package lpc.javaTools.media;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FFmpegUtils {
	public static boolean logInfo = false;
	
	protected static final Logger logger = LogManager.getLogger("FFmpegUtils");
	
	protected static void deleteTempFile(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			LogManager.getLogger().warn("Failed to delete temp file: {}", path.toAbsolutePath().toString());
		}
	}
	
	protected static void runFFmpeg(List<String> cmd, Path outputPath) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);
		
		if (outputPath != null) {
			pb.redirectOutput(outputPath.toFile());
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

	protected static void startStderrConsumer(Process p) {
		new Thread(() -> {
			try (InputStream err = p.getErrorStream()) {
				byte[] buf = new byte[1024];
				while (err.read(buf) != -1) {
					// Discard
				}
			} catch (IOException e) {
				logger.warn("Error consuming stderr", e);
			}
		}).start();
	}
	
	protected static byte[] readAllBytes(Path path) throws IOException {
		try (InputStream in = new FileInputStream(path.toFile())) {
			return in.readAllBytes();
		}
	}
}
