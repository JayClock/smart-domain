package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.Entity;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;

public class Listing implements Entity<String, ListingDescription> {
  private String identity;
  private ListingDescription description;

  public Listing(String identity, ListingDescription description) {
    this.identity = identity;
    this.description = description;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public ListingDescription getDescription() {
    return description;
  }
}
