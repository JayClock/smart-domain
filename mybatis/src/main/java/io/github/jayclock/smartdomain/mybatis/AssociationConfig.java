package io.github.jayclock.smartdomain.mybatis;

import io.github.jayclock.smartdomain.core.InternalApi;

/**
 * Internal association field configuration discovered from {@link AssociationMapping}.
 *
 * @param fieldName field name in entity
 * @param associationType association implementation class
 * @param parentIdField parent ID field name
 * @param eager whether eager loading
 */
@InternalApi
public record AssociationConfig(
    String fieldName, Class<?> associationType, String parentIdField, boolean eager) {}
