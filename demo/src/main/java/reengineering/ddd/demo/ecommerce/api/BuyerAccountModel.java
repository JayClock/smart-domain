package reengineering.ddd.demo.ecommerce.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import reengineering.ddd.demo.ecommerce.model.BuyerAccount;

public class BuyerAccountModel extends RepresentationModel<BuyerAccountModel> {
  private final String id;
  private final String label;
  private final List<PurchaseModel> purchases;

  private BuyerAccountModel(String id, String label, List<PurchaseModel> purchases) {
    this.id = id;
    this.label = label;
    this.purchases = purchases;
  }

  public static BuyerAccountModel of(BuyerAccount account) {
    String selfHref = EcommerceApiTemplates.buyerAccount(account.getIdentity()).build().getPath();
    String purchasesHref =
        EcommerceApiTemplates.createPurchase(account.getIdentity()).build().getPath();
    BuyerAccountModel model =
        new BuyerAccountModel(
            account.getIdentity(),
            account.getDescription().label(),
            account.purchases().findAll().stream()
                .map(
                    purchase ->
                        PurchaseModel.of(
                            purchase,
                            EcommerceApiTemplates.purchase(
                                    account.getIdentity(), purchase.getIdentity())
                                .build()
                                .getPath()))
                .toList());
    model.add(Link.of(selfHref).withSelfRel());
    model.add(
        Affordances.of(Link.of(purchasesHref).withRel("purchases"))
            .afford(HttpMethod.POST)
            .withInput(EcommerceApi.CreatePurchaseRequest.class)
            .withName("create-purchase")
            .toLink());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public List<PurchaseModel> getPurchases() {
    return purchases;
  }
}
