package reengineering.ddd.demo.accounting.memory;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.Many;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class MemoryAssociation<ID, E extends Entity<ID, ?>>
    implements Many<E>, HasMany<ID, E> {

  @Override
  public final Many<E> findAll() {
    return this;
  }

  @Override
  public final Optional<E> findByIdentity(ID identifier) {
    return snapshot().stream()
        .filter(entity -> entity.getIdentity().equals(identifier))
        .findFirst();
  }

  @Override
  public final int size() {
    return snapshot().size();
  }

  @Override
  public final Many<E> subCollection(int from, int to) {
    return new FixedMany<>(snapshot().subList(from, to));
  }

  @Override
  public final Iterator<E> iterator() {
    return snapshot().iterator();
  }

  protected abstract List<E> snapshot();

  private static final class FixedMany<E extends Entity<?, ?>> implements Many<E> {
    private final List<E> list;

    private FixedMany(List<E> list) {
      this.list = list;
    }

    @Override
    public int size() {
      return list.size();
    }

    @Override
    public Many<E> subCollection(int from, int to) {
      return new FixedMany<>(list.subList(from, to));
    }

    @Override
    public Iterator<E> iterator() {
      return list.iterator();
    }
  }
}
