package lpc.javaTools.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ClipboardUtils {
	/**
	 * 将字符串复制到剪贴板
	 */
	public static void copyToClipboard(String text) {
		try {
			// 获取系统剪贴板
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			
			// 创建字符串选择器
			StringSelection selection = new StringSelection(text);
			
			// 设置剪贴板内容
			clipboard.setContents(selection, null);
			
			System.out.println("已复制到剪贴板: " + text);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("复制到剪贴板失败", e);
		}
	}
	
	/**
	 * 从剪贴板获取字符串
	 */
	public static String getFromClipboard() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			
			// 获取剪贴板内容
			Transferable transferable = clipboard.getContents(null);
			
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 清空剪贴板
	 */
	public static void clearClipboard() {
		copyToClipboard("");  // 设置空字符串
	}
}
