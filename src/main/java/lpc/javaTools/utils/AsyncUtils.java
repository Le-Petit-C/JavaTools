package lpc.javaTools.utils;

import lpc.javaTools.media.video.FFmpegVideoUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
	
	public static <T, U, V> CompletableFuture<V> thenCombineAsync(CompletableFuture<T> task, CompletableFuture<U> task1, BiFunction<T, U, V> combiner, Executor executor) {
		if(executor == null) return task.thenCombineAsync(task1, combiner);
		else return task.thenCombineAsync(task1, combiner, executor);
	}
	
	// CompletableFuture中一些Async功能的Nullable Executor化
	// executor为null时表示使用CompletableFuture中的默认executor
	// （没办法，CompletableFuture没给获取默认executor的方法，只能如此“曲线救国”）
	// 想要在“当前线程”执行可以输入下面这个TrampolineExecutor
	// 最好不要输入Runnable::run，容易爆栈
	private static final class TrampolineExecutor implements Executor {
		private static final ThreadLocal<ArrayDeque<Runnable>> QUEUE =
			ThreadLocal.withInitial(ArrayDeque::new);
		private static final ThreadLocal<Boolean> RUNNING =
			ThreadLocal.withInitial(() -> false);
		@Override public void execute(@NonNull Runnable command) {
			ArrayDeque<Runnable> queue = QUEUE.get();
			queue.addLast(command);
			if (RUNNING.get()) return;
			RUNNING.set(true);
			try {
				while (!queue.isEmpty())
					queue.removeFirst().run();
			} finally {
				RUNNING.set(false);
			}
		}
	}
	
	public static final Executor TRAMPOLINE = new TrampolineExecutor();
	
	public static CompletableFuture<Void> runAsync(Runnable runnable, @Nullable Executor executor) {
		if(executor == null) return CompletableFuture.runAsync(runnable);
		else return CompletableFuture.runAsync(runnable, executor);
	}
	
	public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, @Nullable Executor executor) {
		if(executor == null) return CompletableFuture.supplyAsync(supplier);
		else return CompletableFuture.supplyAsync(supplier, executor);
	}
	
	public static CompletableFuture<Void> thenRunAsync(CompletableFuture<?> future, Runnable runnable, @Nullable Executor executor) {
		if(executor == null) return future.thenRunAsync(runnable);
		else return future.thenRunAsync(runnable, executor);
	}
	
	public static <T, U> CompletableFuture<U> thenApplyAsync(CompletableFuture<T> future, Function<T, U> function, @Nullable Executor executor) {
		if(executor == null) return future.thenApplyAsync(function);
		else return future.thenApplyAsync(function, executor);
	}
	
	public static <T, U> CompletableFuture<U> thenComposeAsync(CompletableFuture<T> future, Function<T, CompletableFuture<U>> function, @Nullable Executor executor) {
		if(executor == null) return future.thenComposeAsync(function);
		else return future.thenComposeAsync(function, executor);
	}
	
	public static <T> CompletableFuture<T> whenCompleteAsync(CompletableFuture<T> future, BiConsumer<? super T, ? super Throwable> action, @Nullable Executor executor) {
		if(executor == null) return future.whenCompleteAsync(action);
		else return future.whenCompleteAsync(action, executor);
	}
	
	public static boolean isDirectExecutor(Executor executor) {
		if (executor == null) return false;
		
		Thread thread = Thread.currentThread();
		AtomicBoolean sameThread = new AtomicBoolean(false);
		
		executor.execute(() -> sameThread.set(Thread.currentThread() == thread));
		
		return sameThread.get();
	}
}
