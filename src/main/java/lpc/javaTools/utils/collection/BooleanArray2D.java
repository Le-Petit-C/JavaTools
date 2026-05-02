package lpc.javaTools.utils.collection;

public class BooleanArray2D implements IArray2D<Boolean> {
	private final boolean[] data;
	private final int width, height;
	public BooleanArray2D(int width, int height) {
		this.data = new boolean[width * height];
		this.width = width;
		this.height = height;
	}
	public boolean getShort(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setShort(int x, int y, boolean value) { data[getRawIndex(x, y)] = value; }
	public boolean replaceInt(int x, int y, boolean value) {
		int index = getRawIndex(x, y);
		boolean oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public boolean[] getRawBooleanArray() { return data; }
	@Override @Deprecated public Boolean get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Boolean value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Boolean replace(int x, int y, Boolean value) {
		int index = getRawIndex(x, y);
		boolean oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
