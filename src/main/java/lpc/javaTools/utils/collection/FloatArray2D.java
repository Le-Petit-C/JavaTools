package lpc.javaTools.utils.collection;

public class FloatArray2D implements IArray2D<Float> {
	private final float[] data;
	private final int width, height;
	public FloatArray2D(int width, int height) {
		this.data = new float[width * height];
		this.width = width;
		this.height = height;
	}
	public float getFloat(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setFloat(int x, int y, float value) { data[getRawIndex(x, y)] = value; }
	public void addFloat(int x, int y, float value) { data[getRawIndex(x, y)] += value; }
	public float replaceFloat(int x, int y, float value) {
		int index = getRawIndex(x, y);
		float oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public float[] getRawFloatArray() { return data; }
	@Override @Deprecated public Float get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Float value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Float replace(int x, int y, Float value) {
		int index = getRawIndex(x, y);
		float oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
