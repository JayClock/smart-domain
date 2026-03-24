package reengineering.ddd.demo.ecommerce.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.ecommerce.model.Purchase;

public class PurchaseModel extends RepresentationModel<PurchaseModel> {
  private final String id;
  private final String productName;
  private final int quantity;

  private PurchaseModel(String id, String productName, int quantity) {
    this.id = id;
    this.productName = productName;
    this.quantity = quantity;
  }

  public static PurchaseModel of(Purchase purchase, String href) {
    PurchaseModel model =
        new PurchaseModel(
            purchase.getIdentity(),
            purchase.getDescription().productName(),
            purchase.getDescription().quantity());
    model.add(Link.of(href).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getProductName() {
    return productName;
  }

  public int getQuantity() {
    return quantity;
  }
}
