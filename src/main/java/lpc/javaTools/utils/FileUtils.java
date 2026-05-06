package lpc.javaTools.utils;

import java.io.File;
import java.util.function.BiFunction;

public class FileUtils {
	// ext应该是带'.'的
	public static File nextNotExist(String pathWithoutExt, String ext) {
		File file = new File(pathWithoutExt + ext);
		int i = 0;
		while (file.exists()) file = new File(pathWithoutExt + '(' + ++i + ')' + ext);
		return file;
	}
	
	public static File nextNotExist(String path) {
		return splitExt(path, FileUtils::nextNotExist);
	}
	
	public static File nextNotExistWithSuffix(String path, String suffix) {
		return splitExt(path, (name, ext) -> nextNotExist(name + suffix, ext));
	}
	
	public static <T> T splitExt(String path, BiFunction<String, String, T> callback) {
		int i = path.lastIndexOf('.');
		if(i == -1) return callback.apply(path, "");
		else return callback.apply(path.substring(0, i), path.substring(i));
	}
	
	public static String pathWithSuffix(String path, String suffix) {
		return splitExt(path, (name, ext) -> name + suffix + ext);
	}
}
