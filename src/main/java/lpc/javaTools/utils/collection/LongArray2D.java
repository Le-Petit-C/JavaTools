package lpc.javaTools.utils.collection;

public class LongArray2D implements IArray2D<Long> {
	private final long[] data;
	private final int width, height;
	public LongArray2D(int width, int height) {
		this.data = new long[width * height];
		this.width = width;
		this.height = height;
	}
	public long getShort(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setShort(int x, int y, long value) { data[getRawIndex(x, y)] = value; }
	public long replaceInt(int x, int y, long value) {
		int index = getRawIndex(x, y);
		long oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public long[] getRawLongArray() { return data; }
	@Override @Deprecated public Long get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Long value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Long replace(int x, int y, Long value) {
		int index = getRawIndex(x, y);
		long oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
