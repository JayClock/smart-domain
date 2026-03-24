package reengineering.ddd.demo.ecommerce.memory;

import java.util.List;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;
import reengineering.ddd.demo.ecommerce.model.BuyerAccount;
import reengineering.ddd.demo.ecommerce.model.Purchase;

public class BuyerPurchases extends MemoryAssociation<String, Purchase>
    implements BuyerAccount.Purchases {
  private final InMemoryBuyerAccounts store;
  private final String accountId;

  public BuyerPurchases(InMemoryBuyerAccounts store, String accountId) {
    this.store = store;
    this.accountId = accountId;
  }

  @Override
  public Purchase add(PurchaseDescription description) {
    return store.createPurchase(accountId, description);
  }

  @Override
  protected List<Purchase> snapshot() {
    return store.purchasesOf(accountId);
  }
}
