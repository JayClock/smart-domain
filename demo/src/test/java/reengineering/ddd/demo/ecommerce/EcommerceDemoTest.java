package reengineering.ddd.demo.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import reengineering.ddd.demo.ecommerce.api.InMemorySalesMappers;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.description.PurchaseDescription;
import reengineering.ddd.demo.ecommerce.description.UserDescription;
import reengineering.ddd.demo.ecommerce.memory.BuyerPurchases;
import reengineering.ddd.demo.ecommerce.memory.DefaultPurchasingContext;
import reengineering.ddd.demo.ecommerce.memory.InMemoryBuyerAccounts;
import reengineering.ddd.demo.ecommerce.memory.InMemoryUsers;
import reengineering.ddd.demo.ecommerce.mybatis.DefaultSalesContext;
import reengineering.ddd.demo.ecommerce.mybatis.MybatisSellerStores;
import reengineering.ddd.demo.ecommerce.mybatis.SellerListings;

class EcommerceDemoTest {

  @Test
  void should_switch_user_into_buyer_and_seller_roles() {
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

    var purchase = buyer.buy(new PurchaseDescription("Notebook", 2));
    var listing = seller.sell(new ListingDescription("Mechanical Keyboard", 12, 499));

    assertInstanceOf(BuyerPurchases.class, buyer.account().purchases());
    assertInstanceOf(SellerListings.class, seller.store().listings());
    assertEquals("Notebook", purchase.getDescription().productName());
    assertEquals("Mechanical Keyboard", listing.getDescription().productName());
    assertEquals(1, buyer.purchases().findAll().size());
    assertEquals(1, seller.listings().findAll().size());
  }
}
