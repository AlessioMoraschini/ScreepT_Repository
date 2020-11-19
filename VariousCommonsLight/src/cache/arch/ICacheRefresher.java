package cache.arch;

@FunctionalInterface
public interface ICacheRefresher<T> {
	public T getRefreshedObject(IResultCacheInput input) throws Exception;
}
