package reengineering.ddd.demo.ecommerce.memory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.description.BuyerAccountDescription;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;
import reengineering.ddd.demo.ecommerce.model.BuyerAccount;
import reengineering.ddd.demo.ecommerce.model.BuyerAccounts;
import reengineering.ddd.demo.ecommerce.model.Purchase;
import reengineering.ddd.demo.ecommerce.model.User;

@Component
public class InMemoryBuyerAccounts implements BuyerAccounts {
  private final Map<String, BuyerAccountRecord> accounts = new LinkedHashMap<>();
  private final Map<String, PurchaseRecord> purchases = new LinkedHashMap<>();

  private int nextAccountId = 1;
  private int nextPurchaseId = 1;

  @Override
  public BuyerAccount open(User actor) {
    return findByActor(actor.getIdentity())
        .orElseGet(
            () -> {
              String identity = String.valueOf(nextAccountId++);
              accounts.put(
                  identity,
                  new BuyerAccountRecord(
                      identity,
                      actor.getIdentity(),
                      new BuyerAccountDescription(actor.getDescription().name() + " Buyer")));
              return rehydrateAccount(identity);
            });
  }

  @Override
  public Optional<BuyerAccount> findByActor(String actorId) {
    return accounts.values().stream()
        .filter(record -> record.actorId().equals(actorId))
        .findFirst()
        .map(record -> rehydrateAccount(record.identity()));
  }

  Purchase createPurchase(String accountId, PurchaseDescription description) {
    String identity = String.valueOf(nextPurchaseId++);
    purchases.put(identity, new PurchaseRecord(identity, accountId, description));
    return rehydratePurchase(identity);
  }

  List<Purchase> purchasesOf(String accountId) {
    return purchases.values().stream()
        .filter(record -> record.accountId().equals(accountId))
        .map(record -> rehydratePurchase(record.identity()))
        .toList();
  }

  private BuyerAccount rehydrateAccount(String identity) {
    BuyerAccountRecord record = accounts.get(identity);
    return new BuyerAccount(
        record.identity(), record.description(), new BuyerPurchases(this, record.identity()));
  }

  private Purchase rehydratePurchase(String identity) {
    PurchaseRecord record = purchases.get(identity);
    return new Purchase(record.identity(), record.description());
  }

  private record BuyerAccountRecord(
      String identity, String actorId, BuyerAccountDescription description) {}

  private record PurchaseRecord(
      String identity, String accountId, PurchaseDescription description) {}
}
