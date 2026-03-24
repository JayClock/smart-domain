package io.github.jayclock.smartdomain.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/** Primary entry point for enabling Smart Domain MyBatis integration in Spring Boot apps. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SmartDomainMybatisRegistrar.class, SmartDomainMybatisAutoConfiguration.class})
public @interface EnableSmartDomainMybatis {
  String[] associationBasePackages();

  Class<?>[] leafEntityTypes() default {};
}
