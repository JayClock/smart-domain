package io.github.jayclock.smartdomain.mybatis.memory;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasOne;

/** In-memory {@link HasOne} reference used during eager hydration. */
public class Reference<E extends Entity<?, ?>> implements HasOne<E> {
  protected E entity;

  public Reference() {}

  public Reference(E entity) {
    this.entity = entity;
  }

  @Override
  public E get() {
    return entity;
  }
}
