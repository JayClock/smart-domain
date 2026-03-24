package io.github.jayclock.smartdomain.core;

public interface HasOne<E extends Entity<?, ?>> {
  E get();
}
