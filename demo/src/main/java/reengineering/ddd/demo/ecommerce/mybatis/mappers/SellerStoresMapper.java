package reengineering.ddd.demo.ecommerce.mybatis.mappers;

import io.github.jayclock.smartdomain.mybatis.support.IdHolder;
import reengineering.ddd.demo.ecommerce.description.SellerStoreDescription;

public interface SellerStoresMapper {
  SellerStoreRow findStoreById(int id);

  SellerStoreRow findStoreByActorId(int actorId);

  void insertStore(IdHolder holder, int actorId, SellerStoreDescription description);

  record SellerStoreRow(int id, String name) {}
}
