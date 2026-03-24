package reengineering.ddd.demo.ecommerce.model;

import java.util.Optional;

public interface BuyerAccounts {
  BuyerAccount open(User actor);

  Optional<BuyerAccount> findByActor(String actorId);
}
