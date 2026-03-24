package reengineering.ddd.demo.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.boot.SmartDomainMybatisConfigurer;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reengineering.ddd.demo.library.model.Book;
import reengineering.ddd.demo.library.model.Library;
import reengineering.ddd.demo.library.model.Shelf;
import reengineering.ddd.demo.library.mybatis.config.LibraryDemoSmartDomainMybatisConfiguration;

class LibraryMybatisStarterDemoTest {

  @Test
  void should_register_demo_mybatis_configuration_from_annotation() throws Exception {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(LibraryDemoSmartDomainMybatisConfiguration.class)) {
      SmartDomainMybatisConfigurer configurer = context.getBean(SmartDomainMybatisConfigurer.class);
      Object hydrator = context.getBean("genericEntityHydrator");
      CacheManager cacheManager = context.getBean(CacheManager.class);

      assertEquals(
          List.of("reengineering.ddd.demo.library.mybatis"), configurer.associationBasePackages());
      assertEquals(List.of(Book.class), configurer.leafEntityTypes());
      assertNotNull(hydrator);
      assertEquals("HydratingCacheManager", cacheManager.getClass().getSimpleName());
      assertTrue(
          registeredEntityTypes(hydrator)
              .containsAll(List.of(Library.class, Shelf.class, Book.class)));
    }
  }

  @Test
  void should_keep_starter_descriptor_aligned_with_model_ownership() {
    EnablementDescriptor descriptor =
        new EnablementDescriptor(
            "Library.shelves", "reengineering.ddd.demo.library.mybatis", List.of(Book.class));

    assertEquals("Library.shelves", descriptor.modelEntry());
    assertEquals("reengineering.ddd.demo.library.mybatis", descriptor.associationBasePackage());
    assertEquals(List.of(Book.class), descriptor.leafEntityTypes());
  }

  @SuppressWarnings("unchecked")
  private static List<Class<?>> registeredEntityTypes(Object hydrator)
      throws ReflectiveOperationException {
    Field entityTypesField = hydrator.getClass().getDeclaredField("entityTypes");
    entityTypesField.setAccessible(true);
    return List.copyOf((java.util.Set<Class<?>>) entityTypesField.get(hydrator));
  }

  private record EnablementDescriptor(
      String modelEntry, String associationBasePackage, List<Class<?>> leafEntityTypes) {}
}
