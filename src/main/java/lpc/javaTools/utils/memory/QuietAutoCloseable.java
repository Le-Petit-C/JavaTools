package lpc.javaTools.utils.memory;

public interface QuietAutoCloseable extends AutoCloseable {
	@Override void close();
}
