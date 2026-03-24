package reengineering.ddd.demo.ecommerce.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;

public interface Buyer extends ContextRole<User, BuyerAccount> {
  BuyerAccount account();

  default HasMany<String, Purchase> purchases() {
    return account().purchases();
  }

  default Purchase buy(PurchaseDescription description) {
    return account().buy(description);
  }
}
