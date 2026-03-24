package io.github.jayclock.smartdomain.mybatis;

import io.github.jayclock.smartdomain.mybatis.memory.Reference;
import io.github.jayclock.smartdomain.mybatis.support.InjectableObjectFactory;
import io.github.jayclock.smartdomain.persistence.AbstractReflectiveEntityHydrator;
import io.github.jayclock.smartdomain.persistence.CacheEntry;
import io.github.jayclock.smartdomain.persistence.EntityMetadata;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * MyBatis-oriented {@link AbstractReflectiveEntityHydrator} that discovers association adapters by
 * scanning {@link AssociationMapping} annotations.
 */
public class GenericEntityHydrator extends AbstractReflectiveEntityHydrator<AssociationConfig> {

  private static final String MEMORY_ENTITY_LIST_CLASS =
      "io.github.jayclock.smartdomain.mybatis.memory.EntityList";

  private final Supplier<InjectableObjectFactory> objectFactorySupplier;
  private final List<String> associationBasePackages;
  private final Set<Class<?>> leafEntityTypes;
  private final Map<Class<?>, List<AssociationConfig>> registry = new ConcurrentHashMap<>();
  private final Set<Class<?>> entityTypes = ConcurrentHashMap.newKeySet();

  public GenericEntityHydrator(
      Supplier<InjectableObjectFactory> objectFactorySupplier,
      List<String> associationBasePackages,
      List<Class<?>> leafEntityTypes) {
    this.objectFactorySupplier = objectFactorySupplier;
    this.associationBasePackages = List.copyOf(associationBasePackages);
    this.leafEntityTypes = new LinkedHashSet<>(leafEntityTypes);
  }

  public GenericEntityHydrator(
      InjectableObjectFactory objectFactory,
      List<String> associationBasePackages,
      List<Class<?>> leafEntityTypes) {
    this(() -> objectFactory, associationBasePackages, leafEntityTypes);
  }

  @PostConstruct
  void scanAssociations() {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(AssociationMapping.class));

    for (String basePackage : associationBasePackages) {
      Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);
      for (BeanDefinition bd : candidates) {
        try {
          Class<?> associationClass = Class.forName(bd.getBeanClassName());
          AssociationMapping mapping = associationClass.getAnnotation(AssociationMapping.class);

          if (mapping != null) {
            Class<?> entityType = mapping.entity();
            AssociationConfig config =
                new AssociationConfig(
                    mapping.field(), associationClass, mapping.parentIdField(), mapping.eager());

            registry.computeIfAbsent(entityType, k -> new ArrayList<>()).add(config);
            entityTypes.add(entityType);
          }
        } catch (ClassNotFoundException e) {
          throw new IllegalStateException(
              "Failed to load association class: " + bd.getBeanClassName(), e);
        }
      }
    }

    registerLeafEntities();
  }

  private void registerLeafEntities() {
    for (Class<?> leafEntityType : leafEntityTypes) {
      entityTypes.add(leafEntityType);
      registry.putIfAbsent(leafEntityType, List.of());
    }
  }

  @Override
  protected boolean isManagedEntityType(Class<?> entityType) {
    return entityTypes.contains(entityType);
  }

  @Override
  protected List<AssociationConfig> associationConfigsFor(Class<?> entityType) {
    return registry.getOrDefault(entityType, List.of());
  }

  @Override
  protected String associationFieldName(AssociationConfig config) {
    return config.fieldName();
  }

  @Override
  protected EntityMetadata.AssociationFieldMeta buildAssociationFieldMeta(
      Field entityField, AssociationConfig config) {
    try {
      Field parentIdField = config.associationType().getDeclaredField(config.parentIdField());
      parentIdField.setAccessible(true);

      Field listField = null;
      if (config.eager()) {
        listField = findListField(config.associationType());
        if (listField != null) {
          listField.setAccessible(true);
        }
      }

      return new EntityMetadata.AssociationFieldMeta(
          entityField.getName(),
          config.associationType(),
          parentIdField,
          config.eager(),
          listField,
          false,
          null);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "Failed to build association metadata for " + entityField.getName(), e);
    }
  }

  @Override
  protected EntityMetadata.AssociationFieldMeta buildHasOneFieldMeta(Field entityField) {
    try {
      Field hasOneEntityField = Reference.class.getDeclaredField("entity");
      hasOneEntityField.setAccessible(true);
      return new EntityMetadata.AssociationFieldMeta(
          entityField.getName(), Reference.class, null, true, null, true, hasOneEntityField);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "Failed to build has-one metadata for " + entityField.getName(), e);
    }
  }

  @Override
  protected boolean isEagerAssociationObject(Object association) {
    return isMemoryEntityList(association);
  }

  @Override
  protected List<?> extractEagerAssociationEntities(Object association) {
    return extractListFromMemoryEntityList(association);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Object createLazyAssociation(
      EntityMetadata.AssociationFieldMeta meta, Object parentId) {
    try {
      InjectableObjectFactory factory = objectFactorySupplier.get();
      Object association = factory.create((Class<Object>) meta.associationType());

      if (parentId instanceof Integer intId) {
        meta.parentIdField().setInt(association, intId);
      } else if (parentId instanceof Long longId) {
        meta.parentIdField().setLong(association, longId);
      } else {
        meta.parentIdField().set(association, parentId);
      }

      return association;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create lazy association", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Object createEagerAssociation(
      EntityMetadata.AssociationFieldMeta meta,
      Map<String, List<CacheEntry<?, ?>>> nestedCollections) {
    try {
      InjectableObjectFactory factory = objectFactorySupplier.get();
      Object association = factory.create((Class<Object>) meta.associationType());

      List<CacheEntry<?, ?>> nestedEntries =
          nestedCollections.getOrDefault(meta.fieldName(), List.of());

      List<Object> hydratedEntities = new ArrayList<>();
      for (CacheEntry<?, ?> nestedEntry : nestedEntries) {
        hydratedEntities.add(hydrate(nestedEntry));
      }

      if (meta.listField() != null) {
        meta.listField().set(association, hydratedEntities);
      }

      return association;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create eager association", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Object createHasOneAssociation(
      EntityMetadata.AssociationFieldMeta meta,
      Map<String, List<CacheEntry<?, ?>>> nestedCollections) {
    try {
      InjectableObjectFactory factory = objectFactorySupplier.get();
      Object association = factory.create((Class<Object>) meta.associationType());
      List<CacheEntry<?, ?>> nestedEntries =
          nestedCollections.getOrDefault(meta.fieldName(), List.of());
      if (!nestedEntries.isEmpty() && meta.hasOneEntityField() != null) {
        Object entity = hydrate(nestedEntries.get(0));
        meta.hasOneEntityField().set(association, entity);
      }
      return association;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to create has-one association", e);
    }
  }

  private boolean isMemoryEntityList(Object obj) {
    if (obj == null) {
      return false;
    }

    Class<?> clazz = obj.getClass();
    while (clazz != null) {
      if (clazz.getName().equals(MEMORY_ENTITY_LIST_CLASS)) {
        return true;
      }
      clazz = clazz.getSuperclass();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private List<?> extractListFromMemoryEntityList(Object memoryEntityList) {
    try {
      Field listField = findListField(memoryEntityList.getClass());
      if (listField != null) {
        listField.setAccessible(true);
        return (List<?>) listField.get(memoryEntityList);
      }
      return List.of();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to extract list from memory.EntityList", e);
    }
  }

  private Field findListField(Class<?> clazz) {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("list");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  @Override
  protected Object toInternalIdentity(Object identity) {
    if (identity instanceof String strId) {
      try {
        return Integer.parseInt(strId);
      } catch (NumberFormatException e) {
        return strId;
      }
    }
    return identity;
  }
}
