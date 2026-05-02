package lpc.javaTools.utils.collection;

public class Array3D<T> implements IArray3D<T> {
	private final Object[] data;
	private final int width, height, depth;
	
	public Array3D(int width, int height, int depth) {
		this.data = new Object[width * height * depth];
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	@SuppressWarnings("unchecked") @Override
	public T get(int x, int y, int z) { return (T) data[getRawIndex(x, y, z)]; }
	@Override public void set(int x, int y, int z, T value) { data[getRawIndex(x, y, z)] = value; }
	
	@SuppressWarnings("unchecked") @Override
	public T replace(int x, int y, int z, T value) {
		int index = getRawIndex(x, y, z);
		T oldValue = (T) data[index];
		data[index] = value;
		return oldValue;
	}
	
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	@Override public int getDepth() { return depth; }
}
