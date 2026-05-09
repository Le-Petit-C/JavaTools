package lpc.javaTools.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

public class FileUtils {
	// ext应该是带'.'的
	public static Path nextNotExist(String pathWithoutExt, String ext) {
		Path path = Path.of(pathWithoutExt + ext);
		int i = 0;
		while (Files.exists(path)) path = Path.of(pathWithoutExt + '(' + ++i + ')' + ext);
		return path;
	}
	
	public static Path nextNotExist(String path) {
		return splitExt(path, FileUtils::nextNotExist);
	}
	
	public static Path nextNotExistWithSuffix(String path, String suffix) {
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
