package reengineering.ddd.demo.ecommerce.mybatis;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.model.SalesContext;
import reengineering.ddd.demo.ecommerce.model.Seller;
import reengineering.ddd.demo.ecommerce.model.SellerStore;
import reengineering.ddd.demo.ecommerce.model.SellerStores;
import reengineering.ddd.demo.ecommerce.model.User;

@Component
public class DefaultSalesContext implements SalesContext {
  private final SellerStores stores;

  public DefaultSalesContext(SellerStores stores) {
    this.stores = stores;
  }

  @Override
  public Optional<Seller> switchTo(User actor, SellerStore context) {
    SellerStore store = stores.findByActor(actor.getIdentity()).orElse(null);
    if (store == null || !store.getIdentity().equals(context.getIdentity())) {
      return Optional.empty();
    }
    return Optional.of(new DefaultSeller(actor, store));
  }

  private record DefaultSeller(User actor, SellerStore context) implements Seller {
    @Override
    public SellerStore store() {
      return context;
    }
  }
}
