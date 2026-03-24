package io.github.jayclock.smartdomain.core;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Many<E extends Entity<?, ?>> extends Iterable<E> {
  int size();

  Many<E> subCollection(int from, int to);

  default Stream<E> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
