package reengineering.ddd.demo.ecommerce;

import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;
import reengineering.ddd.demo.ecommerce.description.UserDescription;
import reengineering.ddd.demo.ecommerce.api.InMemorySalesMappers;
import reengineering.ddd.demo.ecommerce.memory.DefaultPurchasingContext;
import reengineering.ddd.demo.ecommerce.memory.InMemoryBuyerAccounts;
import reengineering.ddd.demo.ecommerce.memory.InMemoryUsers;
import reengineering.ddd.demo.ecommerce.mybatis.DefaultSalesContext;
import reengineering.ddd.demo.ecommerce.mybatis.MybatisSellerStores;

public class EcommerceDemoMain {
  public static void main(String[] args) {
    InMemoryUsers users = new InMemoryUsers();
    InMemoryBuyerAccounts buyerAccounts = new InMemoryBuyerAccounts();
    InMemorySalesMappers salesMappers = new InMemorySalesMappers();

    var user = users.create(new UserDescription("Alex"));
    var buyerAccount = buyerAccounts.open(user);
    var sellerStore = new MybatisSellerStores(salesMappers, salesMappers).open(user);

    var buyer = new DefaultPurchasingContext(buyerAccounts).require(user, buyerAccount);
    var seller =
        new DefaultSalesContext(new MybatisSellerStores(salesMappers, salesMappers))
            .require(user, sellerStore);

    buyer.buy(new PurchaseDescription("Notebook", 2));
    seller.sell(new ListingDescription("Mechanical Keyboard", 12, 499));

    System.out.println("Buyer role: " + buyer.getClass().getSimpleName());
    System.out.println("Seller role: " + seller.getClass().getSimpleName());
    System.out.println("Purchases adapter: " + buyer.account().purchases().getClass().getSimpleName());
    System.out.println("Listings adapter: " + seller.store().listings().getClass().getSimpleName());
  }
}
