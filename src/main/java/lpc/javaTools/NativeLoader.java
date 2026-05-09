package lpc.javaTools;

public class NativeLoader {
	public static void init() {}
	
	private static native void nativeInitialize();
	
	static {
		String path = System.getProperty("user.dir") + "/build/native/lpcNatives.dll";
		System.load(path);
		nativeInitialize();
	}
}
