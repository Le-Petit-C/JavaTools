package lpc.javaTools.utils.collection;

// depth-width-height存储顺序的FloatArray3D
public class DWHFloatArray3D extends FloatArray3D {
	public DWHFloatArray3D(int width, int height, int depth) { super(width, height, depth); }
	@Override public int getRawIndexUnchecked(int x, int y, int z) {
		return (y * getWidth() + x) * getDepth() + z;
	}
}
