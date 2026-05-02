package lpc.javaTools.utils.collection;

public interface IArray3D<T> {
	T get(int x, int y, int z);
	void set(int x, int y, int z, T value);
	T replace(int x, int y, int z, T value);
	int getWidth();
	int getHeight();
	int getDepth();
	
	default boolean isOutOfBounds(int x, int y, int z) {
		return x < 0 || y < 0 || z < 0 || x >= getWidth() || y >= getHeight() || z >= getDepth();
	}
	
	default int getRawIndex(int x, int y, int z) {
		if (isOutOfBounds(x, y, z))
			throw new IndexOutOfBoundsException("Index out of bounds: (" + x + ", " + y + "," + z + ")");
		else return getRawIndexUnchecked(x, y, z);
	}
	
	default int getRawIndexUnchecked(int x, int y, int z) {
		return (z * getHeight() + y) * getWidth() + x;
	}
}
