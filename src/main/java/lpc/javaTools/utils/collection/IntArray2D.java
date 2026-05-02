package lpc.javaTools.utils.collection;

public class IntArray2D implements IArray2D<Integer> {
	private final int[] data;
	private final int width, height;
	public IntArray2D(int width, int height) {
		this.data = new int[width * height];
		this.width = width;
		this.height = height;
	}
	public int getInt(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setInt(int x, int y, int value) { data[getRawIndex(x, y)] = value; }
	public int replaceInt(int x, int y, int value) {
		int index = getRawIndex(x, y);
		int oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public int[] getRawIntArray() { return data; }
	@Override @Deprecated public Integer get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Integer value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Integer replace(int x, int y, Integer value) {
		int index = getRawIndex(x, y);
		int oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
