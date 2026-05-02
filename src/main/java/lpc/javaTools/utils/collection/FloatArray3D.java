package lpc.javaTools.utils.collection;

public class FloatArray3D implements IArray3D<Float> {
	protected final float[] data;
	protected final int width, height, depth;
	public FloatArray3D(int width, int height, int depth) {
		this.data = new float[width * height * depth];
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	public float getFloat(int x, int y, int z) { return data[getRawIndex(x, y, z)]; }
	public float getFloatOrDefault(int x, int y, int z, float defaultValue) {
		if(isOutOfBounds(x, y, z)) return defaultValue;
		else return data[getRawIndexUnchecked(x, y, z)];
	}
	public void setFloat(int x, int y, int z, float value) { data[getRawIndex(x, y, z)] = value; }
	public void addFloat(int x, int y, int z, float value) { data[getRawIndex(x, y, z)] += value; }
	public float replaceFloat(int x, int y, int z, float value) {
		int index = getRawIndex(x, y, z);
		float oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	@Override public int getDepth() { return depth; }
	public float[] getRawFloatArray() { return data; }
	@Override @Deprecated public Float get(int x, int y, int z) { return data[getRawIndex(x, y, z)]; }
	@Override @Deprecated public void set(int x, int y, int z, Float value) { data[getRawIndex(x, y, z)] = value; }
	@Override @Deprecated public Float replace(int x, int y, int z, Float value) {
		int index = getRawIndex(x, y, z);
		float oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
