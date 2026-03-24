package reengineering.ddd.demo.ecommerce.memory;

import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.model.Buyer;
import reengineering.ddd.demo.ecommerce.model.BuyerAccount;
import reengineering.ddd.demo.ecommerce.model.BuyerAccounts;
import reengineering.ddd.demo.ecommerce.model.PurchasingContext;
import reengineering.ddd.demo.ecommerce.model.User;

@Component
public class DefaultPurchasingContext implements PurchasingContext {
  private final BuyerAccounts accounts;

  public DefaultPurchasingContext(BuyerAccounts accounts) {
    this.accounts = accounts;
  }

  @Override
  public Optional<Buyer> switchTo(User actor, BuyerAccount context) {
    BuyerAccount account = accounts.findByActor(actor.getIdentity()).orElse(null);
    if (account == null || !account.getIdentity().equals(context.getIdentity())) {
      return Optional.empty();
    }
    return Optional.of(new DefaultBuyer(actor, account));
  }

  private record DefaultBuyer(User actor, BuyerAccount context) implements Buyer {
    @Override
    public BuyerAccount account() {
      return context;
    }
  }
}
