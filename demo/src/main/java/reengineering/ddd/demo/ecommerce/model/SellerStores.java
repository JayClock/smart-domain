package reengineering.ddd.demo.ecommerce.model;

import java.util.Optional;

public interface SellerStores {
  SellerStore open(User actor);

  Optional<SellerStore> findByActor(String actorId);
}
