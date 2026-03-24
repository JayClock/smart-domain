package reengineering.ddd.demo.ecommerce.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.ecommerce.model.SellerStore;

public class SellerStoreModel extends RepresentationModel<SellerStoreModel> {
  private final String id;
  private final String name;
  private final List<ListingModel> listings;

  private SellerStoreModel(String id, String name, List<ListingModel> listings) {
    this.id = id;
    this.name = name;
    this.listings = listings;
  }

  public static SellerStoreModel of(
      SellerStore store, String selfHref, String listingBaseHref) {
    SellerStoreModel model =
        new SellerStoreModel(
            store.getIdentity(),
            store.getDescription().name(),
            store.listings().findAll().stream()
                .map(
                    listing ->
                        ListingModel.of(
                            listing, listingBaseHref + "/listings/" + listing.getIdentity()))
                .toList());
    model.add(Link.of(selfHref).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<ListingModel> getListings() {
    return listings;
  }
}
