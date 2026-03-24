package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.description.SellerStoreDescription;

public class SellerStore implements Entity<String, SellerStoreDescription> {
  private String identity;
  private SellerStoreDescription description;
  private Listings listings;

  public SellerStore(String identity, SellerStoreDescription description, Listings listings) {
    this.identity = identity;
    this.description = description;
    this.listings = listings;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public SellerStoreDescription getDescription() {
    return description;
  }

  public HasMany<String, Listing> listings() {
    return listings;
  }

  public Listing sell(ListingDescription description) {
    return listings.add(description);
  }

  public interface Listings extends HasMany<String, Listing> {
    Listing add(ListingDescription description);
  }
}
