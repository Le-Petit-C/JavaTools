package lpc.javaTools.utils.collection;

public class ShortArray2D implements IArray2D<Short> {
	private final short[] data;
	private final int width, height;
	public ShortArray2D(int width, int height) {
		this.data = new short[width * height];
		this.width = width;
		this.height = height;
	}
	public short getShort(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setShort(int x, int y, short value) { data[getRawIndex(x, y)] = value; }
	public short replaceInt(int x, int y, short value) {
		int index = getRawIndex(x, y);
		short oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public short[] getRawShortArray() { return data; }
	@Override @Deprecated public Short get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Short value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Short replace(int x, int y, Short value) {
		int index = getRawIndex(x, y);
		short oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
