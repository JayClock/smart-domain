package io.github.jayclock.smartdomain.persistence;

import io.github.jayclock.smartdomain.core.InternalApi;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** Internal serialized cache envelope used by hydrators. */
@InternalApi
public record CacheEntry<ID, D>(
    Class<?> entityType,
    ID identity,
    D description,
    Object internalId,
    Map<String, List<CacheEntry<?, ?>>> nestedCollections)
    implements Serializable {

  private static final long serialVersionUID = 2L;
}
