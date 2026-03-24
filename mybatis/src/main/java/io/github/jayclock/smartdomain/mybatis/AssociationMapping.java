package io.github.jayclock.smartdomain.mybatis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares how an association implementation maps back to a managed entity field. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AssociationMapping {
  Class<?> entity();

  String field();

  String parentIdField();

  boolean eager() default false;
}
