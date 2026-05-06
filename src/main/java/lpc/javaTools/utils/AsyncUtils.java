package lpc.javaTools.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class AsyncUtils {
	public static <T> CompletableFuture<T>
	combineResults(List<CompletableFuture<T>> futures, BiFunction<T, T, T> combiner) {
		if(futures.isEmpty()) return CompletableFuture.completedFuture(null);
		return combineResults(futures, combiner, 0, futures.size());
	}
	
	public static <T, U extends Collection<T>> CompletableFuture<U>
	combineCollections(List<CompletableFuture<U>> futures) {
		return combineResults(futures, (c1, c2)->{c1.addAll(c2); c2.clear(); return c1;});
	}
	
	private static <T> CompletableFuture<T>
	combineResults(List<CompletableFuture<T>> futures, BiFunction<T, T, T> combiner, int min, int max) {
		if(max - min <= 1) return futures.get(min);
		else {
			int mid = (min + max) >>> 1;
			return combineResults(futures, combiner, min, mid)
				.thenCombine(combineResults(futures, combiner, mid, max), combiner);
		}
	}
}
