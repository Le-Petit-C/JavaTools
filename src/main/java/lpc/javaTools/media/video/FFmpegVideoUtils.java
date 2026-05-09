package lpc.javaTools.media.video;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lpc.javaTools.media.FFmpegUtils;
import lpc.javaTools.media.audio.PCMData;
import lpc.javaTools.media.image.ImageData;
import lpc.javaTools.utils.AsyncUtils;
import lpc.javaTools.utils.SystemInfo;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * Video processing utilities using FFmpeg pipes.
 */
public class FFmpegVideoUtils extends FFmpegUtils {

	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(FFmpegVideoUtils.class);
	
	public static CompletableFuture<Void> decodeVideoAsync(Path input, Function<VideoInfo, FrameProcessor> processorGenerator, @Nullable Executor executor, int maxTaskCount) throws IOException {
		VideoInfo info = probeVideo(input);
		FrameProcessor processor = processorGenerator.apply(info);
		
		// Start decoder process
		ProcessBuilder pb = new ProcessBuilder(List.of(
			"ffmpeg",
			"-i", input.toString(),
			"-f", "rawvideo",
			"-pix_fmt", "rgba",
			"-vsync", "0",
			"-"
		));
		pb.redirectError(ProcessBuilder.Redirect.PIPE);
		Process decoder = pb.start();
		
		// Consume stderr
		startStderrConsumer(decoder);
        
        InputStream decoderOut = decoder.getInputStream();
		
        AsyncDecodeProcess asyncDecodeProcess = new AsyncDecodeProcess(processor, decoderOut, executor, maxTaskCount, info);
        return AsyncUtils.whenCompleteAsync(asyncDecodeProcess.finalTask, (_, _)->{
            // Wait for decoder
            try {
                int code = decoder.waitFor();
                if (code != 0) throw new RuntimeException("Decoder ffmpeg failed with code " + code);
                decoderOut.close();
                decoder.destroyForcibly();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }}, executor);
	}
	
	public static CompletableFuture<Void> decodeVideoAsync(String input, Function<VideoInfo, FrameProcessor> processorGenerator, Executor executor) throws IOException {
		return decodeVideoAsync(Path.of(input), processorGenerator, executor, SystemInfo.CPUThreadCount * 2);
	}
	
	public static CompletableFuture<Void> decodeVideoAsync(String input, Function<VideoInfo, FrameProcessor> processorGenerator, ThreadPoolExecutor executor) throws IOException {
		return decodeVideoAsync(Path.of(input), processorGenerator, executor, executor.getMaximumPoolSize() * 2);
	}
	
	public static CompletableFuture<Void> decodeVideoAsync(String input, Function<VideoInfo, FrameProcessor> processorGenerator) throws IOException {
		return decodeVideoAsync(Path.of(input), processorGenerator, null, (SystemInfo.CPUThreadCount - 1) * 2);
	}
	
	public static void decodeVideo(String input, Function<VideoInfo, FrameProcessor> processorGenerator) throws IOException {
		decodeVideoAsync(Path.of(input), processorGenerator, AsyncUtils.TRAMPOLINE, 1).join();
	}
	
	public static void decodeVideo(Path input, Function<VideoInfo, FrameProcessor> processorGenerator) throws IOException {
		decodeVideoAsync(input, processorGenerator, AsyncUtils.TRAMPOLINE, 1).join();
	}
	
	private static final class AsyncProcessBuf {
		final byte[] buf;
		final ImageData imageData;
		int frameIndex;

		private AsyncProcessBuf(byte[] buf, ImageData imageData) {
			this.buf = buf;
			this.imageData = imageData;
		}
	}

	private static final class AsyncDecodeProcess {
		private final ArrayDeque<CompletableFuture<AsyncProcessBuf>> taskQueue = new ArrayDeque<>();
		private final FrameProcessor processor;
		private final InputStream decoderOut;
		private final @Nullable Executor executor;
		private final CompletableFuture<Void> finalTask;
		private int frameIndex;
		private CompletableFuture<Void> dependedTask;

		private AsyncDecodeProcess(
			FrameProcessor processor, InputStream decoderOut, @Nullable Executor executor,
			int maxTaskCount, VideoInfo videoInfo
		) {
			this.processor = processor;
			this.decoderOut = decoderOut;
			this.executor = executor;
			int frameSize = videoInfo.width() * videoInfo.height() * 4; // RGBA
			CompletableFuture<?>[] finalTasks = new CompletableFuture<?>[maxTaskCount];
			synchronized (this) {
				dependedTask = CompletableFuture.completedFuture(null);
				for(int i = 0; i < finalTasks.length; ++i) {
					AsyncProcessBuf buf = new AsyncProcessBuf(new byte[frameSize], new ImageData(videoInfo.width(), videoInfo.height()));
					CompletableFuture<AsyncProcessBuf> rootTask = CompletableFuture.completedFuture(buf);
					taskQueue.add(rootTask);
					finalTasks[i] = AsyncUtils.thenComposeAsync(CompletableFuture.completedFuture(buf), this::processFrame1, executor);
				}
			}
			finalTask = CompletableFuture.allOf(finalTasks);
		}

		CompletableFuture<AsyncProcessBuf> processFrame1(AsyncProcessBuf taskBuf) {
			CompletableFuture<AsyncProcessBuf> rootTask;
			try {
				synchronized (this) {
					if(!readFully(decoderOut, taskBuf.buf))
						return CompletableFuture.completedFuture(taskBuf);
					taskBuf.frameIndex = this.frameIndex++;
					CompletableFuture<Void> dependedTask = this.dependedTask
                        = AsyncUtils.thenCombineAsync(this.dependedTask,taskQueue.remove(), (_, _) -> null, executor);
					rootTask = AsyncUtils.thenApplyAsync(dependedTask, _->processFrame2(taskBuf), executor);
					taskQueue.add(rootTask);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return AsyncUtils.thenComposeAsync(rootTask, this::processFrame1, executor);
		}

		AsyncProcessBuf processFrame2(AsyncProcessBuf taskBuf) {
			bytesToImageData(taskBuf.buf, taskBuf.imageData);
			processor.processNextFrame(taskBuf.frameIndex, taskBuf.imageData);
			return taskBuf;
		}
	}

	/**
	 * Creates a video encoder for the given output path and video info.
	 *
	 * @param output Path to output video file
	 * @param info   Video information (width, height, fps)
	 * @return A VideoEncoder instance
	 * @throws IOException If encoder creation fails
	 */
	public static VideoEncoder encodeVideo(Path output, VideoInfo info) throws IOException {
		return new FFmpegVideoEncoder(output, info);
	}

	/**
	 * Probes video information using ffprobe.
	 */
	public static VideoInfo probe(Path videoFile) throws IOException {
		return probeVideo(videoFile);
	}

	/**
	 * Probes video information using ffprobe.
	 */
	private static VideoInfo probeVideo(Path videoFile) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(
			"ffprobe",
			"-v", "quiet",
			"-print_format", "json",
			"-show_streams",
			"-show_format",
			videoFile.toString()
		);
		pb.redirectError(ProcessBuilder.Redirect.INHERIT);

		Process p = pb.start();
		String json;
		try (InputStream in = p.getInputStream()) {
			json = new String(in.readAllBytes());
		}

		int code;
		try {
			code = p.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException(e);
		}
		if (code != 0) {
			throw new IOException("ffprobe failed with code " + code);
		}

		Gson gson = new Gson();
		ProbeResult result = gson.fromJson(json, ProbeResult.class);

		// Find video stream
		Stream videoStream = null;
		for (Stream s : result.streams) {
			if ("video".equals(s.codecType)) {
				videoStream = s;
				break;
			}
		}
		if (videoStream == null) {
			throw new IOException("No video stream found");
		}

		int width = videoStream.width;
		int height = videoStream.height;
		double fps = parseFrameRate(videoStream.rFrameRate);
		double duration = result.format != null && result.format.duration != null
			? Double.parseDouble(result.format.duration) : 0.0;

		return new VideoInfo(width, height, fps, duration);
	}

	private static double parseFrameRate(String rFrameRate) {
		if (rFrameRate == null) return 30.0;
		String[] parts = rFrameRate.split("/");
		if (parts.length == 2) {
			try {
				double num = Double.parseDouble(parts[0]);
				double den = Double.parseDouble(parts[1]);
				return den != 0 ? num / den : 30.0;
			} catch (NumberFormatException e) {
				return 30.0;
			}
		}
		return 30.0;
	}

	/**
	 * Reads exactly len bytes from input stream into buffer.
	 */
	private static boolean readFully(InputStream in, byte[] buffer) throws IOException {
		int offset = 0;
		int len = buffer.length;
		while (offset < len) {
			int read = in.read(buffer, offset, len - offset);
			if (read == -1) {
				return false; // EOF
			}
			offset += read;
		}
		return true;
	}

	/**
	 * Converts RGBA byte[] (0-255) to ImageData float[] (0-1).
	 */
	private static void bytesToImageData(byte[] bytes, ImageData data) {
		float[] floats = data.getRawFloatArray();
		for (int i = 0; i < bytes.length; i++) {
			floats[i] = Byte.toUnsignedInt(bytes[i]) / 255.0f;
		}
	}

	/**
	 * Converts ImageData float[] (0-1) to RGBA byte[] (0-255).
	 */
	private static void imageDataToBytes(ImageData data, byte[] bytes) {
		float[] floats = data.getRawFloatArray();
		for (int i = 0; i < floats.length; i++) {
			bytes[i] = (byte) Math.round(floats[i] * 255.0f);
		}
	}

	// Probe result classes
	private static class ProbeResult {
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
		List<Stream> streams;
		Format format;
	}

	private static class Stream {
		@SerializedName("codec_type")
		String codecType;
		Integer width;
		Integer height;
		@SerializedName("r_frame_rate")
		String rFrameRate;
	}

	private static class Format {
		String duration;
	}
	
	/**
	 * FFmpeg-based video encoder implementation.
	 */
	private static class FFmpegVideoEncoder implements VideoEncoder {
	
		private final Process process;
		private final OutputStream encoderIn;
		private final VideoInfo info;
		private volatile int frameIndex = 0;
		private boolean closed = false;
		private final ArrayList<byte[]> unusedFrameCaches = new ArrayList<>();
		private final Int2ObjectOpenHashMap<byte[]> frameQueue = new Int2ObjectOpenHashMap<>();
	
		FFmpegVideoEncoder(Path output, VideoInfo info) throws IOException {
			this.info = info;
			
			ProcessBuilder pb = new ProcessBuilder(List.of(
				"ffmpeg",
				"-f", "rawvideo",
				"-pix_fmt", "rgba",
				"-s", info.width() + "x" + info.height(),
				"-r", String.valueOf(info.fps()),
				"-i", "-",
				"-c:v", "libx264",
				"-preset", "medium",
				"-crf", "20",
				output.toString()
			));
			pb.redirectError(ProcessBuilder.Redirect.PIPE);
			process = pb.start();
			encoderIn = process.getOutputStream();
	
			// Consume stderr
			startStderrConsumer(process);
		}
	
		@Override public void setFrame(int frameIndex, ImageData frame) {
			if (closed) throw new IllegalStateException("Encoder is closed");
			
			if(frame.getWidth() != info.width() || frame.getHeight() != info.height())
				throw new IllegalArgumentException("Frame width and/or height mismatch!");
			
			if(frameIndex < 0) throw new IllegalArgumentException("Frame index less than 0");
	
			try {
				byte[] byteBuf;
				synchronized (unusedFrameCaches) {
					if(!unusedFrameCaches.isEmpty()) byteBuf = unusedFrameCaches.getLast();
					else byteBuf = new byte[info.width() * info.height() * 4];
				}
				imageDataToBytes(frame, byteBuf);
				synchronized (frameQueue) {
					if(frameQueue.get(frameIndex) != null || frameIndex < this.frameIndex)
						throw new IllegalArgumentException("Repeated frame!");
					else frameQueue.put(frameIndex, byteBuf);
					while(frameQueue.containsKey(this.frameIndex)) {
						byte[] byteBuf2 = frameQueue.remove(this.frameIndex++);
						encoderIn.write(byteBuf2);
						synchronized (unusedFrameCaches) {
							unusedFrameCaches.add(byteBuf2);
						}
					}
					if(frameIndex > this.frameIndex)
						encoderIn.flush();
				}
			} catch (IOException e) {
				throw new RuntimeException("Failed to write frame", e);
			}
		}
	
		@Override
		public void close() {
			if (closed) return;
			
			if(!frameQueue.isEmpty())
				throw new IllegalStateException("Frame queue is not cleared!");
			
			closed = true;
	
			try {
				encoderIn.close();
				int code = process.waitFor();
				if (code != 0) {
					logger.warn("Encoder ffmpeg failed with code {}", code);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.warn("Interrupted while waiting for encoder", e);
			} catch (IOException e) {
				logger.warn("Error closing encoder", e);
			} finally {
				process.destroyForcibly();
			}
		}
	
		@Override
		public void closeWithAudio(PCMData audioData, boolean cutIfAudioTooLong) {
			// TODO: Implement audio muxing if needed
			close();
		}
	
		private static void imageDataToBytes(ImageData data, byte[] bytes) {
			float[] floats = data.getRawFloatArray();
			for (int i = 0; i < floats.length; i++) {
				bytes[i] = (byte) Math.round(floats[i] * 255.0f);
			}
		}
	}
}
