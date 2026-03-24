package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import reengineering.ddd.demo.ecommerce.description.BuyerAccountDescription;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;

public class BuyerAccount implements Entity<String, BuyerAccountDescription> {
  private String identity;
  private BuyerAccountDescription description;
  private Purchases purchases;

  public BuyerAccount(String identity, BuyerAccountDescription description, Purchases purchases) {
    this.identity = identity;
    this.description = description;
    this.purchases = purchases;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public BuyerAccountDescription getDescription() {
    return description;
  }

  public HasMany<String, Purchase> purchases() {
    return purchases;
  }

  public Purchase buy(PurchaseDescription description) {
    return purchases.add(description);
  }

  public interface Purchases extends HasMany<String, Purchase> {
    Purchase add(PurchaseDescription description);
  }
}
