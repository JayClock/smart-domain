package reengineering.ddd.demo.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.boot.SmartDomainMybatisConfigurer;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.model.Transaction;
import reengineering.ddd.demo.accounting.mybatis.config.AccountingDemoSmartDomainMybatisConfiguration;

class AccountingMybatisStarterDemoTest {

  @Test
  void should_register_accounting_demo_mybatis_configuration_from_annotation() throws Exception {
    try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(
            AccountingDemoSmartDomainMybatisConfiguration.class)) {
      SmartDomainMybatisConfigurer configurer = context.getBean(SmartDomainMybatisConfigurer.class);
      Object hydrator = context.getBean("genericEntityHydrator");
      CacheManager cacheManager = context.getBean(CacheManager.class);

      assertEquals(
          List.of("reengineering.ddd.demo.accounting.mybatis"),
          configurer.associationBasePackages());
      assertEquals(List.of(Transaction.class), configurer.leafEntityTypes());
      assertNotNull(hydrator);
      assertEquals("HydratingCacheManager", cacheManager.getClass().getSimpleName());
      assertTrue(
          registeredEntityTypes(hydrator).containsAll(List.of(Account.class, Transaction.class)));
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Class<?>> registeredEntityTypes(Object hydrator)
      throws ReflectiveOperationException {
    Field entityTypesField = hydrator.getClass().getDeclaredField("entityTypes");
    entityTypesField.setAccessible(true);
    return List.copyOf((java.util.Set<Class<?>>) entityTypesField.get(hydrator));
  }
}
