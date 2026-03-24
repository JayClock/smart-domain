package io.github.jayclock.smartdomain.persistence;

import io.github.jayclock.smartdomain.core.InternalApi;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

/** Internal reflective constructor metadata for managed entities. */
@InternalApi
public record EntityMetadata(
    Class<?> entityType, Constructor<?> constructor, List<AssociationFieldMeta> associations) {

  public record AssociationFieldMeta(
      String fieldName,
      Class<?> associationType,
      Field parentIdField,
      boolean eager,
      Field listField,
      boolean hasOne,
      Field hasOneEntityField) {}
}
