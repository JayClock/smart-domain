package reengineering.ddd.demo.ecommerce.mybatis;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.ecommerce.description.SellerStoreDescription;
import reengineering.ddd.demo.ecommerce.model.SellerStore;
import reengineering.ddd.demo.ecommerce.model.SellerStores;
import reengineering.ddd.demo.ecommerce.model.User;
import reengineering.ddd.demo.ecommerce.mybatis.mappers.SellerListingsMapper;
import reengineering.ddd.demo.ecommerce.mybatis.mappers.SellerStoresMapper;

@Component
public class MybatisSellerStores implements SellerStores {
  private final SellerStoresMapper storesMapper;
  private final SellerListingsMapper listingsMapper;

  public MybatisSellerStores(SellerStoresMapper storesMapper, SellerListingsMapper listingsMapper) {
    this.storesMapper = storesMapper;
    this.listingsMapper = listingsMapper;
  }

  @Override
  public SellerStore open(User actor) {
    SellerStoresMapper.SellerStoreRow row =
        storesMapper.findStoreByActorId(Integer.parseInt(actor.getIdentity()));
    if (row == null) {
      IdHolder holder = new IdHolder();
      storesMapper.insertStore(
          holder,
          Integer.parseInt(actor.getIdentity()),
          new SellerStoreDescription(actor.getDescription().name() + " Store"));
      row = storesMapper.findStoreById(holder.id());
    }
    return rehydrate(row);
  }

  @Override
  public Optional<SellerStore> findByActor(String actorId) {
    return Optional.ofNullable(storesMapper.findStoreByActorId(Integer.parseInt(actorId)))
        .map(this::rehydrate);
  }

  private SellerStore rehydrate(SellerStoresMapper.SellerStoreRow row) {
    return new SellerStore(
        String.valueOf(row.id()),
        new SellerStoreDescription(row.name()),
        new SellerListings(listingsMapper, row.id()));
  }
}
