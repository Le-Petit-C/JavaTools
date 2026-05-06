package lpc.javaTools.utils;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SystemInfo {
	public static final String desktopPath =  FileSystemView.getFileSystemView().getHomeDirectory().getPath();
	
	public static boolean FMASupport() {
		String os = System.getProperty("os.name").toLowerCase();
		
		try {
			Process process;
			if (os.contains("linux")) {
				process = new ProcessBuilder("grep", "-o", "fma", "/proc/cpuinfo").start();
			} else if (os.contains("mac")) {
				process = new ProcessBuilder("sysctl", "-a").start();
			} else if (os.contains("win")) {
				process = new ProcessBuilder("wmic", "cpu", "get", "caption").start();
			} else {
				return false;
			}
			
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())
			);
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.toLowerCase().contains("fma")) {
					return true;
				}
			}
			
			return process.waitFor() == 0;
			
		} catch (Exception e) {
			return false;
		}
	}
}
