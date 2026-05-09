package lpc.javaTools.media.image.functions;

import lpc.javaTools.media.image.FFmpegImageUtils;
import lpc.javaTools.media.image.ImageData;
import lpc.javaTools.minecraft.replay.Ease;
import lpc.javaTools.utils.ColorUtils;
import lpc.javaTools.utils.algorithm.IterateUtils;
import lpc.javaTools.utils.math.MathUtils;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.IOException;
import java.nio.file.Path;

import static lpc.javaTools.utils.FileUtils.nextNotExistWithSuffix;

public class TrimmingEffect {
	public static void trimTextPicture(
		Path inputPath, Path outputPath,
		float innerRadius, float expandRadius,
		Vector2f basicDirection,
		float[] color1, float[] color2,
		boolean hsbEase, Ease ease
	) throws IOException {
		if(innerRadius < 0 || expandRadius < 0) throw new IllegalArgumentException();
		Vector2f normalizedBasicDirection = basicDirection.normalize(new Vector2f());
		int expanded = (int)Math.ceil(expandRadius);
		ImageData image = FFmpegImageUtils.decodeImage(inputPath);
		ImageData newImage = new ImageData(image.getWidth() + expanded * 2, image.getHeight() + expanded * 2);
		float[] hsbCache = hsbEase ? new float[3] : null;
		float[] rgbCache = new float[3];
		float[] color1hsb = hsbEase ? ColorUtils.RGBtoHSB(color1, new float[3]) : null;
		float[] color2hsb = hsbEase ? ColorUtils.RGBtoHSB(color2, new float[3]) : null;
		Vector2f direction = new Vector2f();
		for(int y = 0; y < newImage.getHeight(); ++y) {
			int srcY = y - expanded;
			for(int x = 0; x < newImage.getWidth(); ++x) {
				int srcX = x - expanded;
				boolean isTranslucent = image.getAlphaOrDefault(srcX, srcY, 0.0f) <= 0.5f;
				float d = Float.POSITIVE_INFINITY;
				direction.set(0, 0);
				for(Vector2i pos : IterateUtils.iterateFromClosestInEuclideanDistance(x, y, (isTranslucent ? expandRadius : innerRadius) + 0.707107f)) {
					float dx = pos.x - x, dy = pos.y - y;
					float distanceSquared = dx * dx + dy * dy;
					float distance = (float)Math.sqrt(distanceSquared);
					float alpha = image.getAlphaOrDefault(pos.x - expanded, pos.y - expanded, 0.0f);
					float d1;
					if(isTranslucent) {
						if(alpha > 0) d1 = distance + 0.5f - alpha;
						else d1 = Float.POSITIVE_INFINITY;
					}
					else {
						if(alpha < 1) d1 = distance - 0.5f + alpha;
						else d1 = Float.POSITIVE_INFINITY;
					}
					if(d1 < d) d = d1;
					if(distanceSquared > 0) {
						float k = (0.5f - alpha) / distanceSquared;
						direction.add(dx * k, dy * k);
					}
				}
				if(isTranslucent) d *= -1;
				if(d >= innerRadius || d <= -expandRadius) {
					newImage.setPixel(x, y, 0, 0, 0, isTranslucent ? 0 : 1);
					continue;
				}
				float directionLength = direction.length();
				if(directionLength <= 0.00001) continue;
				float k = MathUtils.unlerp(innerRadius, -expandRadius, d);
				float t = ease.translate(0.5f * (normalizedBasicDirection.dot(direction) / directionLength + 1.0f));
				if(hsbEase) ColorUtils.HSBtoRGB(ColorUtils.lerpHSB(color1hsb, color2hsb, t, hsbCache), rgbCache);
				else ColorUtils.lerp(color1, color2, t, rgbCache);
				newImage.setAlpha(x, y, 1.0f - k);
				newImage.setRed(x, y, rgbCache[0] * k);
				newImage.setGreen(x, y, rgbCache[1] * k);
				newImage.setBlue(x, y, rgbCache[2] * k);
			}
		}
		FFmpegImageUtils.encodeImage(newImage, outputPath);
	}
	
	public static void trimTextPicture(
		String filePath,
		float innerRadius, float expandRadius,
		Vector2f basicDirection,
		float[] color1, float[] color2,
		boolean hsbEase, Ease ease
	) throws IOException {
		Path outputPath = nextNotExistWithSuffix(filePath, "_trimmed");
		trimTextPicture(Path.of(filePath), outputPath, innerRadius, expandRadius, basicDirection, color1, color2, hsbEase, ease);
	}
}
