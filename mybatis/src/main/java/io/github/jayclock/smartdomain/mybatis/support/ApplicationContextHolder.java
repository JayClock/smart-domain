package io.github.jayclock.smartdomain.mybatis.support;

import io.github.jayclock.smartdomain.core.InternalApi;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/** Internal bridge for environments where MyBatis creates objects outside Spring. */
@InternalApi
public class ApplicationContextHolder implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ApplicationContextHolder.context = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return context;
  }
}
