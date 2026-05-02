package lpc.javaTools.utils.algorithm;

import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import lpc.javaTools.utils.math.MathUtils;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

import java.util.Iterator;

public class IterateUtils {
	public static Iterable<Vector2i> iterateFromClosestInRadius(float startX, float startY, float radius) {
		float radiusSquared = radius * radius;
		return new Iterable<>() {
			@Override public @NonNull Iterator<Vector2i> iterator() {
				LongPriorityQueue queue = new LongHeapPriorityQueue((o1, o2) -> {
					int o1x = (int)o1, o1y = (int)(o1 >>> 32);
					int o2x = (int)o2, o2y = (int)(o2 >>> 32);
					float d1 = MathUtils.lengthSquared(o1x - startX, o1y - startY);
					float d2 = MathUtils.lengthSquared(o2x - startX, o2y - startY);
					return Float.compare(d1, d2);
				});
				int centerX = Math.round(startX);
				int centerY = Math.round(startY);
				queue.enqueue(((long)centerY << 32) | Integer.toUnsignedLong(centerX));
				return new Iterator<>() {
					final Vector2i nextCache = new Vector2i();
					@Override public boolean hasNext() {
						return !queue.isEmpty();
					}
					
					@Override public Vector2i next() {
						long c = queue.dequeueLong();
						int x = (int) c;
						int y = (int) (c >>> 32);
						nextCache.set(x, y);
						if(y <= centerY) tryAddPos(x, y - 1);
						if(y >= centerY) tryAddPos(x, y + 1);
						if(y != centerY) return nextCache;
						if(x <= centerX) tryAddPos(x - 1, y);
						if(x >= centerX) tryAddPos(x + 1, y);
						return nextCache;
					}
					
					private void tryAddPos(int x, int y) {
						float dx = x - startX;
						float dy = y - startY;
						if(dx * dx + dy * dy > radiusSquared) return;
						queue.enqueue(((long)y << 32) | (x & 0xFFFFFFFFL));
					}
				};
			}
		};
	}
}
