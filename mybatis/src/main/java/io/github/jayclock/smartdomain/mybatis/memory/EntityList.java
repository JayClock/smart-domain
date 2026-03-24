package io.github.jayclock.smartdomain.mybatis.memory;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.Many;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/** In-memory association implementation used for eager hydration and tests. */
public class EntityList<Id, E extends Entity<Id, ?>> implements Many<E>, HasMany<Id, E> {
  protected List<E> list = new ArrayList<>();

  public EntityList() {}

  public EntityList(List<E> list) {
    this.list = list;
  }

  @Override
  public Many<E> findAll() {
    return this;
  }

  @Override
  public Optional<E> findByIdentity(Id identifier) {
    return stream().filter(it -> it.getIdentity().equals(identifier)).findFirst();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public Many<E> subCollection(int from, int to) {
    return new EntityList<>(list.subList(from, to));
  }

  @Override
  public Iterator<E> iterator() {
    return list.iterator();
  }
}
