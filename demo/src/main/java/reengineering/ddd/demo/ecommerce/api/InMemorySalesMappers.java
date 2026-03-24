package reengineering.ddd.demo.ecommerce.api;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.description.ListingDescription;
import reengineering.ddd.demo.ecommerce.description.SellerStoreDescription;
import reengineering.ddd.demo.ecommerce.model.Listing;
import reengineering.ddd.demo.ecommerce.mybatis.mappers.SellerListingsMapper;
import reengineering.ddd.demo.ecommerce.mybatis.mappers.SellerStoresMapper;

@Component
public class InMemorySalesMappers implements SellerStoresMapper, SellerListingsMapper {
  private final Map<Integer, StoreRecord> stores = new LinkedHashMap<>();
  private final Map<Integer, ListingRecord> listings = new LinkedHashMap<>();

  private int nextStoreId = 1;
  private int nextListingId = 1;

  @Override
  public SellerStoreRow findStoreById(int id) {
    StoreRecord record = stores.get(id);
    if (record == null) {
      return null;
    }
    return new SellerStoreRow(record.id(), record.description().name());
  }

  @Override
  public SellerStoreRow findStoreByActorId(int actorId) {
    return stores.values().stream()
        .filter(record -> record.actorId() == actorId)
        .findFirst()
        .map(record -> new SellerStoreRow(record.id(), record.description().name()))
        .orElse(null);
  }

  @Override
  public void insertStore(IdHolder holder, int actorId, SellerStoreDescription description) {
    int id = nextStoreId++;
    stores.put(id, new StoreRecord(id, actorId, description));
    assign(holder, id);
  }

  @Override
  public List<Listing> findListingsByStoreId(int sellerStoreId, int from, int limit) {
    return listings.values().stream()
        .filter(record -> record.sellerStoreId() == sellerStoreId)
        .skip(from)
        .limit(limit)
        .map(record -> new Listing(String.valueOf(record.id()), record.description()))
        .toList();
  }

  @Override
  public Listing findListingByStoreAndId(int sellerStoreId, int listingId) {
    ListingRecord record = listings.get(listingId);
    if (record == null || record.sellerStoreId() != sellerStoreId) {
      return null;
    }
    return new Listing(String.valueOf(record.id()), record.description());
  }

  @Override
  public int countListingsByStore(int sellerStoreId) {
    return (int)
        listings.values().stream().filter(record -> record.sellerStoreId() == sellerStoreId).count();
  }

  @Override
  public void insertListing(IdHolder holder, int sellerStoreId, ListingDescription description) {
    int id = nextListingId++;
    listings.put(id, new ListingRecord(id, sellerStoreId, description));
    assign(holder, id);
  }

  private void assign(IdHolder holder, int id) {
    try {
      var field = IdHolder.class.getDeclaredField("id");
      field.setAccessible(true);
      field.setInt(holder, id);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to assign generated id", exception);
    }
  }

  private record StoreRecord(int id, int actorId, SellerStoreDescription description) {}

  private record ListingRecord(int id, int sellerStoreId, ListingDescription description) {}
}
