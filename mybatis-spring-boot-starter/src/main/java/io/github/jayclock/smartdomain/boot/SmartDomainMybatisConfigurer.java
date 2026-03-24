package io.github.jayclock.smartdomain.boot;

import io.github.jayclock.smartdomain.core.InternalApi;
import java.util.List;

/** Internal descriptor produced from {@link EnableSmartDomainMybatis}. */
@InternalApi
public interface SmartDomainMybatisConfigurer {
  List<String> associationBasePackages();

  List<Class<?>> leafEntityTypes();
}
