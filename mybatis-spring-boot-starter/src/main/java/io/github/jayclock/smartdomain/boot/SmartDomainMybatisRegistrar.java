package io.github.jayclock.smartdomain.boot;

import io.github.jayclock.smartdomain.core.InternalApi;
import java.util.List;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/** Internal registrar that turns {@link EnableSmartDomainMybatis} metadata into beans. */
@InternalApi
public class SmartDomainMybatisRegistrar implements ImportBeanDefinitionRegistrar {

  private static final String CONFIGURER_BEAN_NAME = "smartDomainMybatisConfigurer";

  @Override
  public void registerBeanDefinitions(
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    if (registry.containsBeanDefinition(CONFIGURER_BEAN_NAME)) {
      return;
    }

    var attributes =
        importingClassMetadata.getAnnotationAttributes(EnableSmartDomainMybatis.class.getName());
    if (attributes == null) {
      return;
    }

    String[] associationBasePackages = (String[]) attributes.get("associationBasePackages");
    Class<?>[] leafEntityTypes = (Class<?>[]) attributes.get("leafEntityTypes");

    RootBeanDefinition definition =
        new RootBeanDefinition(AnnotationDrivenSmartDomainMybatisConfigurer.class);
    definition.getConstructorArgumentValues().addIndexedArgumentValue(0, associationBasePackages);
    definition.getConstructorArgumentValues().addIndexedArgumentValue(1, leafEntityTypes);

    registry.registerBeanDefinition(CONFIGURER_BEAN_NAME, definition);
  }

  static final class AnnotationDrivenSmartDomainMybatisConfigurer
      implements SmartDomainMybatisConfigurer {

    private final List<String> associationBasePackages;
    private final List<Class<?>> leafEntityTypes;

    AnnotationDrivenSmartDomainMybatisConfigurer(
        String[] associationBasePackages, Class<?>[] leafEntityTypes) {
      this.associationBasePackages = List.of(associationBasePackages);
      this.leafEntityTypes = List.of(leafEntityTypes);
    }

    @Override
    public List<String> associationBasePackages() {
      return associationBasePackages;
    }

    @Override
    public List<Class<?>> leafEntityTypes() {
      return leafEntityTypes;
    }
  }
}
