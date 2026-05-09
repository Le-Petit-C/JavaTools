package lpc.javaTools.utils.algorithm;

import lpc.javaTools.NativeLoader;
import lpc.javaTools.utils.math.MathHelper;
import lpc.javaTools.utils.math.interfaces.ToBooleanFunction;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class IterateUtils {
	
	static { NativeLoader.init(); }
	
	private static native Object[] getArrayListElementData(ArrayList<?> list);
	private static native void setArrayListSize(ArrayList<?> list, int size);
	private static native void increaseArrayListModCount(ArrayList<?> list);
	
	public static Iterable<Vector2i> iterateFromClosestInEuclideanDistance(float startX, float startY, float radius) {
		float radiusSquared = radius * radius;
		return ()->new IterateFromClosestInEuclideanDistanceIterator(startX, startY, radius, radiusSquared);
	}
	
	// 从近到远遍历，但是只保证遍历顺序在“渲染关系”上由近到远，遍历形状依赖于shouldIterateFurther
	public static Iterable<int[]> iterateFromClosestByRender(int nDimension, ToBooleanFunction<int[]> isAvailable) {
		return ()->new IterateFromClosestByRenderIterator(nDimension, isAvailable);
	}
	
	public static <T> Iterable<T> iterateFromClosestByRender(int nDimension, ToBooleanFunction<T> isAvailable, Function<int[], T> translator) {
		ToBooleanFunction<int[]> _isAvailable = pos->isAvailable.applyAsBoolean(translator.apply(pos));
		return translate(()->new IterateFromClosestByRenderIterator(2, _isAvailable), translator);
	}
	
	public static Iterable<Vector2i> iterateFromClosestByRender2D(ToBooleanFunction<Vector2i> isAvailable) {
		Vector2i resCache = new Vector2i();
		return iterateFromClosestByRender(2, isAvailable, resCache::set);
	}
	
	public static Iterable<Vector3i> iterateFromClosestByRender3D(ToBooleanFunction<Vector3i> isAvailable) {
		Vector3i resCache = new Vector3i();
		return iterateFromClosestByRender(2, isAvailable, resCache::set);
	}
	
	
	// 保序地O(n)移除需要移除的内容
	/*public static <T> void accumulate(ArrayList<T> list, ToBooleanFunction<? super T> shouldRemove) {
		int i = 0;
		while (i < list.size() && !shouldRemove.applyAsBoolean(list.get(i))) ++i;
		int j = i;
		while (++i < list.size()) {
			if(shouldRemove.applyAsBoolean(list.get(i))) continue;
			list.set(j++, list.get(i));
		}
		while(j < list.size()) list.removeLast();
	}*/
	
	@SuppressWarnings("unchecked")
	public static <T> void fastRemove(ArrayList<T> list, ToBooleanFunction<? super T> shouldRemove) {
		increaseArrayListModCount(list);
		int i = 0;
		int size = list.size();
		Object[] elementData = getArrayListElementData(list);
		while (i < size && !shouldRemove.applyAsBoolean((T)elementData[i])) ++i;
		int j = i;
		while (++i < size) {
			if(shouldRemove.applyAsBoolean((T)elementData[i])) continue;
			elementData[j++] = elementData[i];
		}
		setArrayListSize(list, j);
		while (j < size) elementData[j++] = null;
	}
	
	public static <T> void fastRemove2(ArrayList<T> list, ToBooleanFunction<? super T> shouldRemove) {
		Collection<T> fakeCollection = new AbstractCollection<>() {
			@Override public int size() { return 0; }
			@SuppressWarnings("unchecked") @Override public boolean contains(Object o) { return shouldRemove.applyAsBoolean((T)o); }
			@Override public @NonNull Iterator<T> iterator() { throw new UnsupportedOperationException(); }
		};
		list.removeAll(fakeCollection);
	}
	
	// 不保序地O(n)移除需要移除的内容，平均来说相较于fastRemove常数会更小一些，移除率更低时常数会更小
	/*public static <T> void fastRemoveUnsorted(ArrayList<T> list, ToBooleanFunction<? super T> shouldRemove) {
		int i = 0;
		while(true) {
			while (i < list.size() && shouldRemove.applyAsBoolean(list.getLast())) list.removeLast();
			int size = list.size();
			while (i < size && !shouldRemove.applyAsBoolean(list.get(i))) ++i;
			if(i >= size) return;
			list.set(i, list.removeLast());
		}
	}*/
	
	@SuppressWarnings("unchecked")
	public static <T> void fastRemoveUnsorted(ArrayList<T> list, ToBooleanFunction<? super T> shouldRemove) {
		increaseArrayListModCount(list);
		int i = 0;
		Object[] elementData = getArrayListElementData(list);
		int size = list.size();
		while(true) {
			while (i < size && shouldRemove.applyAsBoolean((T)elementData[size - 1])) elementData[--size] = null;
			while (i < size && !shouldRemove.applyAsBoolean((T)elementData[i])) ++i;
			if(i >= size) break;
			elementData[i] = elementData[size - 1];
			elementData[--size] = null;
		}
		setArrayListSize(list, size);
	}
	
	public static <T, U> Iterable<U> translate(Iterable<T> iterable, Function<T, U> translator) {
		return ()->new Iterator<>() {
			final Iterator<T> iterator = iterable.iterator();
			@Override public boolean hasNext() { return iterator.hasNext(); }
			@Override public U next() { return translator.apply(iterator.next()); }
		};
	}
	
	static class IterateFromClosestInEuclideanDistanceIterator implements Iterator<Vector2i> {
		final long[] poses;
		final float[] distanceSquares;
		int heapSize = 0;
		final Vector2i nextCache = new Vector2i();
		final float startX, startY;
		final int centerX, centerY;
		final float radiusSquared;
		
		IterateFromClosestInEuclideanDistanceIterator(float startX, float startY, float radius, float radiusSquared) {
			int n = Math.max(MathHelper.ceil(radius) * 8, 1); // 应该有办法做更小的上界估计？
			poses = new long[n];
			distanceSquares = new float[n];
			this.startX = startX;
			this.startY = startY;
			centerX = Math.round(startX);
			centerY = Math.round(startY);
			this.radiusSquared = radiusSquared;
			tryAddPos(centerX, centerY);
		}
		
		@Override public boolean hasNext() {
			return heapSize != 0;
		}
		
		@Override public Vector2i next() {
			long c = popHeap();
			nextCache.set((int)c, (int)(c >>> 32));
			if(nextCache.x <= centerX) tryAddPos(nextCache.x - 1, nextCache.y);
			if(nextCache.x >= centerX) tryAddPos(nextCache.x + 1, nextCache.y);
			if(nextCache.x != centerX) return nextCache;
			if(nextCache.y <= centerY) tryAddPos(nextCache.x, nextCache.y - 1);
			if(nextCache.y >= centerY) tryAddPos(nextCache.x, nextCache.y + 1);
			return nextCache;
		}
		
		private long popHeap() {
			if(heapSize == 0) throw new NoSuchElementException();
			--heapSize;
			long rootPos = poses[0];
			long tailPos = poses[heapSize];
			float tailDistanceSquared = distanceSquares[heapSize];
			int curr = 0;
			while(true) {
				int left = (curr << 1) + 1, right = left + 1;
				if(left < heapSize) {
					int min = right < heapSize ? (distanceSquares[left] <= distanceSquares[right] ? left : right) : left;
					if(distanceSquares[min] < tailDistanceSquared) {
						poses[curr] = poses[min];
						distanceSquares[curr] = distanceSquares[min];
						curr = min;
					}
					else break;
				}
				else break;
			}
			poses[curr] = tailPos;
			distanceSquares[curr] = tailDistanceSquared;
			return rootPos;
		}
		
		private void pushHeap(long pos, float distanceSquared) {
			int curr = heapSize++;
			while(curr > 0) {
				int parent = (curr - 1) >> 1;
				if(distanceSquared >= distanceSquares[parent]) break;
				poses[curr] = poses[parent];
				distanceSquares[curr] = distanceSquares[parent];
				curr = parent;
			}
			poses[curr] = pos;
			distanceSquares[curr] = distanceSquared;
		}
		
		private void tryAddPos(int x, int y) {
			float dx = x - startX;
			float dy = y - startY;
			float distanceSquared = dx * dx + dy * dy;
			if(distanceSquared > radiusSquared) return;
			pushHeap(Integer.toUnsignedLong(x) | (Integer.toUnsignedLong(y) << 32), distanceSquared);
		}
	}
	
	static class IterateFromClosestByRenderIterator implements Iterator<int[]> {
		int[] pos;
		boolean firstNext = true;
		ToBooleanFunction<int[]> isAvailable;
		IterateFromClosestByRenderIterator(int nDimension, ToBooleanFunction<int[]> isAvailable) {
			this.pos = new int[nDimension];
			if(!isAvailable.applyAsBoolean(pos)) pos = null;
			this.isAvailable = isAvailable;
		}
		@Override public boolean hasNext() { return pos != null; }
		@Override public int[] next() {
			int[] next = pos;
			if(firstNext) firstNext = false;
			else prepareNext();
			return next;
		}
		
		private void prepareNext() {
			if(pos == null) throw new NoSuchElementException();
			int i;
			for(i = 0; i < pos.length; ++i) {
				if(pos[i] >= 0) {
					++pos[i];
					if(isAvailable.applyAsBoolean(pos)) break;
					else pos[i] = 0;
				}
				--pos[i];
				if(isAvailable.applyAsBoolean(pos)) break;
				else pos[i] = 0;
			}
			if(i == pos.length) pos = null;
		}
	}
}
