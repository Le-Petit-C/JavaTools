package lpc.javaTools.utils.math;

import lpc.javaTools.utils.SystemInfo;

@SuppressWarnings("unused")
public class MathHelper {
	public static float PI = (float)Math.PI;
	public static float sqrt(float x) { return org.joml.Math.sqrt(x); }
	public static float sin(float val) { return org.joml.Math.sin(val); }
	public static float cos(float val) { return org.joml.Math.cos(val); }
	public static float tan(float val) { return org.joml.Math.tan(val); }
	
	/** @see org.joml.Options */
	private static void staticInit() {
		System.setProperty("joml.fastmath", "true");
		System.setProperty("joml.sinLookup", "true");
		if(SystemInfo.FMASupport())
			System.getProperty("joml.useMathFma", "true");
	}
	static { staticInit(); }
}
