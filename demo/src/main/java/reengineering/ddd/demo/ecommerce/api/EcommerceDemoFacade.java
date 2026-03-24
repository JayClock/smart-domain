package reengineering.ddd.demo.ecommerce.api;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;
import reengineering.ddd.demo.ecommerce.description.UserDescription;
import reengineering.ddd.demo.ecommerce.model.Buyer;
import reengineering.ddd.demo.ecommerce.model.BuyerAccount;
import reengineering.ddd.demo.ecommerce.model.BuyerAccounts;
import reengineering.ddd.demo.ecommerce.model.Listing;
import reengineering.ddd.demo.ecommerce.model.Purchase;
import reengineering.ddd.demo.ecommerce.model.PurchasingContext;
import reengineering.ddd.demo.ecommerce.model.SalesContext;
import reengineering.ddd.demo.ecommerce.model.Seller;
import reengineering.ddd.demo.ecommerce.model.SellerStore;
import reengineering.ddd.demo.ecommerce.model.SellerStores;
import reengineering.ddd.demo.ecommerce.model.User;
import reengineering.ddd.demo.ecommerce.model.Users;

@Component
public class EcommerceDemoFacade {
  private final Users users;
  private final BuyerAccounts buyerAccounts;
  private final SellerStores sellerStores;
  private final PurchasingContext purchasingContext;
  private final SalesContext salesContext;

  private User demoUser;
  private BuyerAccount demoBuyerAccount;
  private SellerStore demoSellerStore;

  public EcommerceDemoFacade(
      Users users,
      BuyerAccounts buyerAccounts,
      SellerStores sellerStores,
      PurchasingContext purchasingContext,
      SalesContext salesContext) {
    this.users = users;
    this.buyerAccounts = buyerAccounts;
    this.sellerStores = sellerStores;
    this.purchasingContext = purchasingContext;
    this.salesContext = salesContext;
  }

  @PostConstruct
  void initialize() {
    demoUser = users.create(new UserDescription("Alex"));
    demoBuyerAccount = buyerAccounts.open(demoUser);
    demoSellerStore = sellerStores.open(demoUser);

    seller().sell(new ListingDescription("Mechanical Keyboard", 12, 499));
    buyer().buy(new PurchaseDescription("Notebook", 2));
  }

  public User user() {
    return demoUser;
  }

  public Buyer buyer() {
    return purchasingContext.require(demoUser, demoBuyerAccount);
  }

  public Seller seller() {
    return salesContext.require(demoUser, demoSellerStore);
  }

  public Purchase buy(String productName, int quantity) {
    return buyer().buy(new PurchaseDescription(productName, quantity));
  }

  public Listing sell(String productName, int inventory, int unitPrice) {
    return seller().sell(new ListingDescription(productName, inventory, unitPrice));
  }

  public BuyerAccount buyerAccount() {
    return buyer().account();
  }

  public SellerStore sellerStore() {
    return seller().store();
  }
}
