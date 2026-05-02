package lpc.javaTools.utils;

import lpc.javaTools.utils.math.MathUtils;

import java.awt.*;

public class ColorUtils {
	public static float[] RGBtoHSB(int r, int g, int b, float[] hsbVals) {
		return Color.RGBtoHSB(r, g, b, hsbVals);
	}
	
	/**
	 * @see java.awt.Color#RGBtoHSB(int, int, int, float[])
	 */
	public static float[] RGBtoHSB(float r, float g, float b, float[] hsbVals) {
		float hue, saturation, brightness;
		if (hsbVals == null) hsbVals = new float[3];
		float cmax = Math.max(r, g);
		if (b > cmax) cmax = b;
		float cmin = Math.min(r, g);
		if (b < cmin) cmin = b;
		brightness = cmax;
		if (cmax != 0)
			saturation = (cmax - cmin) / cmax;
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = (cmax - r) / (cmax - cmin);
			float greenc = (cmax - g) / (cmax - cmin);
			float bluec = (cmax - b) / (cmax - cmin);
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0f + redc - bluec;
			else
				hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}
		hsbVals[0] = hue;
		hsbVals[1] = saturation;
		hsbVals[2] = brightness;
		return hsbVals;
	}
	public static float[] RGBtoHSB(float[] rgb, float[] hsbVals) {
		return RGBtoHSB(rgb[0], rgb[1], rgb[2], hsbVals);
	}
	
	
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		return Color.HSBtoRGB(hue, saturation, brightness);
	}
	
	/**
	 * @see java.awt.Color#HSBtoRGB(float, float, float)
	 */
	public static float[] HSBtoRGB(float hue, float saturation, float brightness, float[] rgbVals) {
		float r, g, b;
		if (rgbVals == null) rgbVals = new float[3];
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0f;
			float f = h - (float)java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
				case 0 -> { r = brightness; g = t; b = p; }
				case 1 -> { r = q; g = brightness; b = p; }
				case 2 -> { r = p; g = brightness; b = t; }
				case 3 -> { r = p; g = q; b = brightness; }
				case 4 -> { r = t; g = p; b = brightness; }
				case 5 -> { r = brightness; g = p; b = q; }
				default -> throw new InternalError("Something went wrong when converting from HSB to RGB. Hue value was " + h);
			}
		}
		rgbVals[0] = r;
		rgbVals[1] = g;
		rgbVals[2] = b;
		return rgbVals;
	}
	public static float[] HSBtoRGB(float[] hsb, float[] rgbVals) {
		return HSBtoRGB(hsb[0], hsb[1], hsb[2], rgbVals);
	}
	
	
	// 在HSB上线性插值
	public static float[] lerpHSB(float[] hsb1, float[] hsb2, float t, float[] res) {
		if(res == null) res = new float[3];
		res[0] = MathUtils.roundLerp(hsb1[0], hsb2[0], t);
		res[1] = MathUtils.lerp(hsb1[1], hsb2[1], t);
		res[2] = MathUtils.lerp(hsb1[2], hsb2[2], t);
		return res;
	}
	
	// 在HSB上线性插值
	public static float[] lerp(float[] rgb1, float[] rgb2, float t, float[] res) {
		if(res == null) res = new float[3];
		res[0] = MathUtils.lerp(rgb1[0], rgb2[0], t);
		res[1] = MathUtils.lerp(rgb1[1], rgb2[1], t);
		res[2] = MathUtils.lerp(rgb1[2], rgb2[2], t);
		return res;
	}
}
