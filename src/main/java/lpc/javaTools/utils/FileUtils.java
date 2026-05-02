package lpc.javaTools.utils;

import java.io.File;

public class FileUtils {
	// ext应该是带'.'的
	public static File nextNotExist(String pathWithoutExt, String ext) {
		File file = new File(pathWithoutExt + ext);
		int i = 0;
		while (file.exists())
			file = new File(pathWithoutExt + '(' + ++i + ')' + ext);
		return file;
	}
}
