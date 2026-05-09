package lpc.javaTools.utils.memory;

import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {
	private final Supplier<T> supplier;
	private T cachedValue = null;
	public CachedSupplier(Supplier<T> supplier) { this.supplier = supplier; }
	@Override public T get() {
		if(cachedValue == null) cachedValue = supplier.get();
		return cachedValue;
	}
}
