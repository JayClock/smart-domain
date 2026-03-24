package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;

public interface Seller extends ContextRole<User, SellerStore> {
  SellerStore store();

  default HasMany<String, Listing> listings() {
    return store().listings();
  }

  default Listing sell(ListingDescription description) {
    return store().sell(description);
  }
}
