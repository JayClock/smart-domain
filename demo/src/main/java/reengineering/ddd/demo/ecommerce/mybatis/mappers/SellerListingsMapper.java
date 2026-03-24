package reengineering.ddd.demo.ecommerce.mybatis.mappers;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import java.util.List;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.model.Listing;

public interface SellerListingsMapper {
  List<Listing> findListingsByStoreId(int sellerStoreId, int from, int limit);

  Listing findListingByStoreAndId(int sellerStoreId, int listingId);

  int countListingsByStore(int sellerStoreId);

  void insertListing(IdHolder holder, int sellerStoreId, ListingDescription description);
}
