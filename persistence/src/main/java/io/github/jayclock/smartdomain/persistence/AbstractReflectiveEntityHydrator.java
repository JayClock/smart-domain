package io.github.jayclock.smartdomain.persistence;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasOne;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for reflective entity hydration strategies.
 *
 * <p>Consumers should extend this type only when building custom persistence integrations.
 */
public abstract class AbstractReflectiveEntityHydrator<C> implements EntityHydrator {

  private final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();

  protected abstract boolean isManagedEntityType(Class<?> entityType);

  protected abstract List<C> associationConfigsFor(Class<?> entityType);

  protected abstract String associationFieldName(C config);

  protected abstract EntityMetadata.AssociationFieldMeta buildAssociationFieldMeta(
      Field entityField, C config);

  protected abstract EntityMetadata.AssociationFieldMeta buildHasOneFieldMeta(Field entityField);

  protected abstract boolean isEagerAssociationObject(Object association);

  protected abstract List<?> extractEagerAssociationEntities(Object association);

  protected abstract Object createLazyAssociation(
      EntityMetadata.AssociationFieldMeta meta, Object parentId);

  protected abstract Object createEagerAssociation(
      EntityMetadata.AssociationFieldMeta meta,
      Map<String, List<CacheEntry<?, ?>>> nestedCollections);

  protected abstract Object createHasOneAssociation(
      EntityMetadata.AssociationFieldMeta meta,
      Map<String, List<CacheEntry<?, ?>>> nestedCollections);

  protected abstract Object toInternalIdentity(Object identity);

  @Override
  public boolean isEntity(Object value) {
    return value != null && isManagedEntityType(value.getClass());
  }

  @Override
  public boolean isEntityList(Object value) {
    if (value instanceof List<?> list && !list.isEmpty()) {
      return isEntity(list.get(0));
    }
    return false;
  }

  @Override
  public boolean isHydratedValue(Object value) {
    return value instanceof CacheEntry;
  }

  @Override
  public boolean isHydratedValueList(Object value) {
    if (value instanceof List<?> list && !list.isEmpty()) {
      return list.get(0) instanceof CacheEntry;
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object dehydrate(Object value) {
    return extract((Entity<?, ?>) value);
  }

  public <E extends Entity<ID, D>, ID, D> CacheEntry<ID, D> extract(E entity) {
    Map<String, List<CacheEntry<?, ?>>> nestedCollections = extractNestedCollections(entity);

    return new CacheEntry<>(
        entity.getClass(),
        entity.getIdentity(),
        entity.getDescription(),
        toInternalIdentity(entity.getIdentity()),
        nestedCollections);
  }

  private Map<String, List<CacheEntry<?, ?>>> extractNestedCollections(Entity<?, ?> entity) {
    EntityMetadata metadata = getOrCreateMetadata(entity.getClass());
    Map<String, List<CacheEntry<?, ?>>> nestedCollections = new HashMap<>();

    for (EntityMetadata.AssociationFieldMeta meta : metadata.associations()) {
      try {
        Field field = entity.getClass().getDeclaredField(meta.fieldName());
        field.setAccessible(true);
        Object association = field.get(entity);

        if (meta.hasOne()) {
          List<CacheEntry<?, ?>> nestedEntries = new ArrayList<>();
          if (association instanceof HasOne<?> hasOne) {
            Object nestedEntity = hasOne.get();
            if (nestedEntity instanceof Entity<?, ?> nested) {
              nestedEntries.add(extract(nested));
            }
          }
          nestedCollections.put(meta.fieldName(), nestedEntries);
          continue;
        }

        if (meta.eager() && isEagerAssociationObject(association)) {
          List<?> nestedEntities = extractEagerAssociationEntities(association);
          List<CacheEntry<?, ?>> nestedEntries = new ArrayList<>();

          for (Object nestedEntity : nestedEntities) {
            if (nestedEntity instanceof Entity<?, ?> nested) {
              nestedEntries.add(extract(nested));
            }
          }

          nestedCollections.put(meta.fieldName(), nestedEntries);
        }
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(
            "Failed to extract nested collection: " + meta.fieldName(), e);
      }
    }

    return nestedCollections;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<CacheEntry<?, ?>> dehydrateList(List<?> entities) {
    List result = new ArrayList<>();
    for (Object e : entities) {
      result.add(extract((Entity<?, ?>) e));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public <E extends Entity<ID, D>, ID, D> E hydrate(CacheEntry<ID, D> entry) {
    Class<E> entityType = (Class<E>) entry.entityType();
    EntityMetadata metadata = getOrCreateMetadata(entityType);

    try {
      Object[] args = new Object[2 + metadata.associations().size()];
      args[0] = entry.identity();
      args[1] = entry.description();

      for (int i = 0; i < metadata.associations().size(); i++) {
        EntityMetadata.AssociationFieldMeta assocMeta = metadata.associations().get(i);

        if (assocMeta.hasOne()) {
          args[2 + i] = createHasOneAssociation(assocMeta, entry.nestedCollections());
        } else if (assocMeta.eager()) {
          args[2 + i] = createEagerAssociation(assocMeta, entry.nestedCollections());
        } else {
          args[2 + i] = createLazyAssociation(assocMeta, entry.internalId());
        }
      }

      return (E) metadata.constructor().newInstance(args);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to hydrate: " + entityType.getName(), e);
    }
  }

  @Override
  public Object hydrate(Object value) {
    return hydrate((CacheEntry<?, ?>) value);
  }

  @Override
  public List<?> hydrateList(List<?> entries) {
    return entries.stream().map(e -> hydrate((CacheEntry<?, ?>) e)).toList();
  }

  private EntityMetadata getOrCreateMetadata(Class<?> entityType) {
    return metadataCache.computeIfAbsent(entityType, this::buildMetadata);
  }

  private EntityMetadata buildMetadata(Class<?> entityType) {
    List<C> configs = associationConfigsFor(entityType);
    Map<String, C> configByFieldName = new HashMap<>();
    for (C config : configs) {
      configByFieldName.put(associationFieldName(config), config);
    }

    try {
      Class<?> descriptionType = entityType.getMethod("getDescription").getReturnType();
      Constructor<?> constructor = findTargetConstructor(entityType, descriptionType);
      List<Field> constructorAssociationFields =
          resolveConstructorAssociationFields(entityType, constructor);

      List<EntityMetadata.AssociationFieldMeta> assocMetas = new ArrayList<>();
      for (Field entityField : constructorAssociationFields) {
        C config = configByFieldName.get(entityField.getName());
        if (config != null) {
          assocMetas.add(buildAssociationFieldMeta(entityField, config));
          continue;
        }

        if (HasOne.class.isAssignableFrom(entityField.getType())) {
          assocMetas.add(buildHasOneFieldMeta(entityField));
          continue;
        }

        throw new IllegalStateException(
            "No association mapping found for constructor field: "
                + entityType.getName()
                + "."
                + entityField.getName());
      }

      return new EntityMetadata(entityType, constructor, assocMetas);

    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to build metadata for: " + entityType.getName(), e);
    }
  }

  private Constructor<?> findTargetConstructor(Class<?> entityType, Class<?> descriptionType)
      throws NoSuchMethodException {
    Constructor<?> targetConstructor = null;
    for (Constructor<?> ctor : entityType.getConstructors()) {
      Class<?>[] paramTypes = ctor.getParameterTypes();
      if (paramTypes.length >= 2
          && paramTypes[0] == String.class
          && paramTypes[1] == descriptionType) {
        if (targetConstructor == null
            || ctor.getParameterCount() > targetConstructor.getParameterCount()) {
          targetConstructor = ctor;
        }
      }
    }
    if (targetConstructor == null) {
      throw new NoSuchMethodException(
          "No matching constructor found for "
              + entityType.getName()
              + " with identity and description parameters");
    }
    return targetConstructor;
  }

  private List<Field> resolveConstructorAssociationFields(
      Class<?> entityType, Constructor<?> constructor) {
    Class<?>[] paramTypes = constructor.getParameterTypes();
    if (paramTypes.length <= 2) {
      return List.of();
    }

    List<Field> candidateFields =
        Arrays.stream(entityType.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> !field.isSynthetic())
            .filter(field -> !"identity".equals(field.getName()))
            .filter(field -> !"description".equals(field.getName()))
            .toList();

    boolean[] used = new boolean[candidateFields.size()];
    List<Field> orderedFields = new ArrayList<>();

    for (int paramIndex = 2; paramIndex < paramTypes.length; paramIndex++) {
      Class<?> paramType = paramTypes[paramIndex];
      int matchedFieldIndex = -1;
      for (int fieldIndex = 0; fieldIndex < candidateFields.size(); fieldIndex++) {
        if (!used[fieldIndex] && candidateFields.get(fieldIndex).getType() == paramType) {
          matchedFieldIndex = fieldIndex;
          break;
        }
      }

      if (matchedFieldIndex < 0) {
        throw new IllegalStateException(
            "Cannot match constructor parameter type "
                + paramType.getName()
                + " for entity "
                + entityType.getName());
      }

      used[matchedFieldIndex] = true;
      orderedFields.add(candidateFields.get(matchedFieldIndex));
    }

    return orderedFields;
  }
}
