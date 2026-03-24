package reengineering.ddd.demo.ecommerce.mybatis;

import io.github.jayclock.smartdomain.mybatis.AssociationMapping;
import io.github.jayclock.smartdomain.mybatis.database.EntityList;
import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import jakarta.inject.Inject;
import java.util.List;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.model.Listing;
import reengineering.ddd.demo.ecommerce.model.SellerStore;
import reengineering.ddd.demo.ecommerce.mybatis.mappers.SellerListingsMapper;

@AssociationMapping(entity = SellerStore.class, field = "listings", parentIdField = "sellerStoreId")
public class SellerListings extends EntityList<String, Listing> implements SellerStore.Listings {
  private int sellerStoreId;
  private SellerListingsMapper mapper;

  public SellerListings() {}

  public SellerListings(SellerListingsMapper mapper, int sellerStoreId) {
    this.mapper = mapper;
    this.sellerStoreId = sellerStoreId;
  }

  @Inject
  public void setMapper(SellerListingsMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  protected List<Listing> findEntities(int from, int to) {
    return mapper.findListingsByStoreId(sellerStoreId, from, to - from);
  }

  @Override
  protected Listing findEntity(String id) {
    return mapper.findListingByStoreAndId(sellerStoreId, Integer.parseInt(id));
  }

  @Override
  public int size() {
    return mapper.countListingsByStore(sellerStoreId);
  }

  @Override
  public Listing add(ListingDescription description) {
    IdHolder holder = new IdHolder();
    mapper.insertListing(holder, sellerStoreId, description);
    return mapper.findListingByStoreAndId(sellerStoreId, holder.id());
  }
}
