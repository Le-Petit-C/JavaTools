package lpc.javaTools.utils.collection;

public class ByteArray2D implements IArray2D<Byte> {
	private final byte[] data;
	private final int width, height;
	public ByteArray2D(int width, int height) {
		this.data = new byte[width * height];
		this.width = width;
		this.height = height;
	}
	public byte getShort(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setShort(int x, int y, byte value) { data[getRawIndex(x, y)] = value; }
	public byte replaceInt(int x, int y, byte value) {
		int index = getRawIndex(x, y);
		byte oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public byte[] getRawByteArray() { return data; }
	@Override @Deprecated public Byte get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Byte value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Byte replace(int x, int y, Byte value) {
		int index = getRawIndex(x, y);
		byte oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
