package io.github.jayclock.smartdomain.persistence;

import java.util.List;

/** Converts managed entities to cache-safe values and restores them on cache reads. */
public interface EntityHydrator {
  boolean isEntity(Object value);

  boolean isEntityList(Object value);

  boolean isHydratedValue(Object value);

  boolean isHydratedValueList(Object value);

  Object dehydrate(Object value);

  List<?> dehydrateList(List<?> values);

  Object hydrate(Object value);

  List<?> hydrateList(List<?> values);
}
