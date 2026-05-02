package lpc.javaTools.utils.collection;

public interface IArray2D<T> {
	T get(int x, int y);
	void set(int x, int y, T value);
	T replace(int x, int y, T value);
	int getWidth();
	int getHeight();
	
	default int getRawIndex(int x, int y) {
		if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			throw new IndexOutOfBoundsException("Index out of bounds: (" + x + ", " + y + ")");
		else return y * getWidth() + x;
	}
}
