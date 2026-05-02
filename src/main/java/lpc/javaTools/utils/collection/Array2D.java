package lpc.javaTools.utils.collection;

public class Array2D<T> implements IArray2D<T> {
	private final Object[] data;
	private final int width, height;
	
	public Array2D(int width, int height) {
		this.data = new Object[width * height];
		this.width = width;
		this.height = height;
	}
	
	@SuppressWarnings("unchecked") @Override
	public T get(int x, int y) { return (T) data[getRawIndex(x, y)]; }
	@Override public void set(int x, int y, T value) { data[getRawIndex(x, y)] = value; }
	
	@SuppressWarnings("unchecked") @Override
	public T replace(int x, int y, T value) {
		int index = getRawIndex(x, y);
		T oldValue = (T) data[index];
		data[index] = value;
		return oldValue;
	}
	
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
}
