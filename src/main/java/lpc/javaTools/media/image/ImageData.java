package lpc.javaTools.media.image;

import lpc.javaTools.utils.collection.DWHFloatArray3D;

public class ImageData extends DWHFloatArray3D {
	public ImageData(int width, int height) { super(width, height, 4); }
	
	public float getRed(int x, int y) { return getFloat(x, y, 0); }
	public float getGreen(int x, int y) { return getFloat(x, y, 1); }
	public float getBlue(int x, int y) { return getFloat(x, y, 2); }
	public float getAlpha(int x, int y) { return getFloat(x, y, 3); }
	
	public float getRedOrDefault(int x, int y, float defaultValue) { return getFloatOrDefault(x, y, 0, defaultValue); }
	public float getGreenOrDefault(int x, int y, float defaultValue) { return getFloatOrDefault(x, y, 1, defaultValue); }
	public float getBlueOrDefault(int x, int y, float defaultValue) { return getFloatOrDefault(x, y, 2, defaultValue); }
	public float getAlphaOrDefault(int x, int y, float defaultValue) { return getFloatOrDefault(x, y, 3, defaultValue); }
	
	public void setRed(int x, int y, float value) { setFloat(x, y, 0, value); }
	public void setGreen(int x, int y, float value) { setFloat(x, y, 1, value); }
	public void setBlue(int x, int y, float value) { setFloat(x, y, 2, value); }
	public void setAlpha(int x, int y, float value) { setFloat(x, y, 3, value); }
	
	public float replaceRed(int x, int y, float value) { return replaceFloat(x, y, 0, value); }
	public float replaceGreen(int x, int y, float value) { return replaceFloat(x, y, 1, value); }
	public float replaceBlue(int x, int y, float value) { return replaceFloat(x, y, 2, value); }
	public float replaceAlpha(int x, int y, float value) { return replaceFloat(x, y, 3, value); }
	
	public void setPixel(int x, int y, float red, float green, float blue, float alpha) {
		int startIndex = getRawIndex(x, y, 0);
		data[startIndex] = red;
		data[startIndex + 1] = green;
		data[startIndex + 2] = blue;
		data[startIndex + 3] = alpha;
	}
	
	public void blendPixel(int x, int y, float red, float green, float blue, float alpha, boolean cover) {
		int startIndex = getRawIndex(x, y, 0);
		float oldAlpha = data[startIndex + 3];
		float newAlpha = data[startIndex + 3] = 1 - (1 - oldAlpha) * (1 - alpha);
		if(newAlpha == 0) return;
		float kSource, kDst;
		if(cover) {
			kSource = alpha / newAlpha;
			kDst = 1 - kSource;
		}
		else {
			kDst = oldAlpha / newAlpha;
			kSource = 1 - kDst;
		}
		data[startIndex] = data[startIndex] * kDst + red * kSource;
		data[startIndex + 1] = data[startIndex + 1] * kDst + green * kSource;
		data[startIndex + 2] = data[startIndex + 2] * kDst + blue * kSource;
	}
	
	public void blendPixel(int x, int y, float red, float green, float blue, float alpha) {
		blendPixel(x, y, red, green, blue, alpha, true);
	}
	
	public boolean isOutOfBounds(int x, int y) {
		return x < 0 || y < 0 || x >= getWidth() || y >= getHeight();
	}
}
