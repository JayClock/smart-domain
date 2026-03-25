package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.Entity;
import reengineering.ddd.demo.accounting.description.OperatorDescription;

public class Operator implements Entity<String, OperatorDescription> {
  private final String identity;
  private final OperatorDescription description;

  public Operator(String identity, OperatorDescription description) {
    this.identity = identity;
    this.description = description;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public OperatorDescription getDescription() {
    return description;
  }
}
