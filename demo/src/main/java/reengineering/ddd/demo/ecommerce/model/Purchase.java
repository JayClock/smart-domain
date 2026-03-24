package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.Entity;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;

public class Purchase implements Entity<String, PurchaseDescription> {
  private String identity;
  private PurchaseDescription description;

  public Purchase(String identity, PurchaseDescription description) {
    this.identity = identity;
    this.description = description;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public PurchaseDescription getDescription() {
    return description;
  }
}
