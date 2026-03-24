package io.github.jayclock.smartdomain.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as implementation detail.
 *
 * <p>Internal API may change between patch releases and is not covered by the public compatibility
 * contract.
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {}
