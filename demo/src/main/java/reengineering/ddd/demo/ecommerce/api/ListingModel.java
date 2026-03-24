package reengineering.ddd.demo.ecommerce.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.ecommerce.model.Listing;

public class ListingModel extends RepresentationModel<ListingModel> {
  private final String id;
  private final String productName;
  private final int inventory;
  private final int unitPrice;

  private ListingModel(String id, String productName, int inventory, int unitPrice) {
    this.id = id;
    this.productName = productName;
    this.inventory = inventory;
    this.unitPrice = unitPrice;
  }

  public static ListingModel of(Listing listing, String href) {
    ListingModel model =
        new ListingModel(
            listing.getIdentity(),
            listing.getDescription().productName(),
            listing.getDescription().inventory(),
            listing.getDescription().unitPrice());
    model.add(Link.of(href).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getProductName() {
    return productName;
  }

  public int getInventory() {
    return inventory;
  }

  public int getUnitPrice() {
    return unitPrice;
  }
}
