package io.github.jayclock.smartdomain.persistence;

import io.github.jayclock.smartdomain.core.InternalApi;
import java.util.List;
import java.util.concurrent.Callable;
import org.springframework.cache.Cache;

/** Internal cache adapter used by {@link HydratingCacheManager}. */
@InternalApi
public class HydratingCache implements Cache {

  private final Cache delegate;
  private final EntityHydrator hydrator;

  public HydratingCache(Cache delegate, EntityHydrator hydrator) {
    this.delegate = delegate;
    this.hydrator = hydrator;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Object getNativeCache() {
    return delegate.getNativeCache();
  }

  @Override
  public ValueWrapper get(Object key) {
    ValueWrapper wrapper = delegate.get(key);
    if (wrapper == null) {
      return null;
    }

    Object value = wrapper.get();

    if (hydrator.isHydratedValue(value)) {
      return () -> hydrator.hydrate(value);
    }

    if (hydrator.isHydratedValueList(value)) {
      return () -> hydrator.hydrateList((List<?>) value);
    }

    return wrapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Object key, Class<T> type) {
    Object value = delegate.get(key, Object.class);
    if (value == null) {
      return null;
    }

    if (hydrator.isHydratedValue(value)) {
      return (T) hydrator.hydrate(value);
    }

    if (hydrator.isHydratedValueList(value)) {
      return (T) hydrator.hydrateList((List<?>) value);
    }

    return (T) value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Object key, Callable<T> valueLoader) {
    return delegate.get(
        key,
        () -> {
          T loaded = valueLoader.call();

          if (hydrator.isEntity(loaded)) {
            return (T) hydrator.dehydrate(loaded);
          }

          if (hydrator.isEntityList(loaded)) {
            return (T) hydrator.dehydrateList((List<?>) loaded);
          }

          return loaded;
        });
  }

  @Override
  public void put(Object key, Object value) {
    if (hydrator.isEntity(value)) {
      delegate.put(key, hydrator.dehydrate(value));
    } else if (hydrator.isEntityList(value)) {
      delegate.put(key, hydrator.dehydrateList((List<?>) value));
    } else {
      delegate.put(key, value);
    }
  }

  @Override
  public void evict(Object key) {
    delegate.evict(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public boolean evictIfPresent(Object key) {
    return delegate.evictIfPresent(key);
  }

  @Override
  public boolean invalidate() {
    return delegate.invalidate();
  }
}
