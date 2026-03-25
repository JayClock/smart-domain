package reengineering.ddd.demo.ecommerce.api;

import java.util.List;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
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

  public static SellerStoreModel of(SellerStore store) {
    String selfHref = EcommerceApiTemplates.sellerStore(store.getIdentity()).build().getPath();
    String listingsHref =
        EcommerceApiTemplates.createListing(store.getIdentity()).build().getPath();
    SellerStoreModel model =
        new SellerStoreModel(
            store.getIdentity(),
            store.getDescription().name(),
            store.listings().findAll().stream()
                .map(
                    listing ->
                        ListingModel.of(
                            listing,
                            EcommerceApiTemplates.listing(
                                    store.getIdentity(), listing.getIdentity())
                                .build()
                                .getPath()))
                .toList());
    model.add(Link.of(selfHref).withSelfRel());
    model.add(
        Affordances.of(Link.of(listingsHref).withRel("listings"))
            .afford(HttpMethod.POST)
            .withInput(EcommerceApi.CreateListingRequest.class)
            .withName("create-listing")
            .toLink());
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
