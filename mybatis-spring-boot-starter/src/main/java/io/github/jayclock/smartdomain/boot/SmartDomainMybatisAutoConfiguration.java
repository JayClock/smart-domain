package io.github.jayclock.smartdomain.boot;

import io.github.jayclock.smartdomain.core.InternalApi;
import io.github.jayclock.smartdomain.mybatis.GenericEntityHydrator;
import io.github.jayclock.smartdomain.mybatis.support.ApplicationContextHolder;
import io.github.jayclock.smartdomain.mybatis.support.InjectableObjectFactory;
import io.github.jayclock.smartdomain.mybatis.support.ObjectFactoryConverter;
import io.github.jayclock.smartdomain.persistence.HydratingCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/** Internal auto-configuration imported by {@link EnableSmartDomainMybatis}. */
@AutoConfiguration
@EnableCaching
@ConditionalOnClass(GenericEntityHydrator.class)
@InternalApi
public class SmartDomainMybatisAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ApplicationContextHolder applicationContextHolder() {
    return new ApplicationContextHolder();
  }

  @Bean
  @ConditionalOnMissingBean
  public InjectableObjectFactory injectableObjectFactory() {
    return new InjectableObjectFactory();
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectFactoryConverter objectFactoryConverter() {
    return new ObjectFactoryConverter();
  }

  @Bean
  @ConditionalOnMissingBean
  public GenericEntityHydrator genericEntityHydrator(
      ApplicationContext context, SmartDomainMybatisConfigurer configurer) {
    return new GenericEntityHydrator(
        () -> context.getBean(InjectableObjectFactory.class),
        configurer.associationBasePackages(),
        configurer.leafEntityTypes());
  }

  @Bean
  @ConditionalOnMissingBean(CacheManager.class)
  public CacheManager cacheManager(GenericEntityHydrator hydrator) {
    return new HydratingCacheManager(hydrator);
  }
}
