package lpc.javaTools.utils.collection;

public class DoubleArray2D implements IArray2D<Double> {
	private final double[] data;
	private final int width, height;
	public DoubleArray2D(int width, int height) {
		this.data = new double[width * height];
		this.width = width;
		this.height = height;
	}
	public double getShort(int x, int y) { return data[getRawIndex(x, y)]; }
	public void setShort(int x, int y, double value) { data[getRawIndex(x, y)] = value; }
	public double replaceInt(int x, int y, double value) {
		int index = getRawIndex(x, y);
		double oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
	@Override public int getWidth() { return width; }
	@Override public int getHeight() { return height; }
	public double[] getRawDoubleArray() { return data; }
	@Override @Deprecated public Double get(int x, int y) { return data[getRawIndex(x, y)]; }
	@Override @Deprecated public void set(int x, int y, Double value) { data[getRawIndex(x, y)] = value; }
	@Override @Deprecated public Double replace(int x, int y, Double value) {
		int index = getRawIndex(x, y);
		double oldValue = data[index];
		data[index] = value;
		return oldValue;
	}
}
