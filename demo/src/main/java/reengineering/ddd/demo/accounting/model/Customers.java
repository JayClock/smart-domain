package reengineering.ddd.demo.accounting.model;

import java.util.Optional;
import reengineering.ddd.demo.accounting.description.AccountDescription;
import reengineering.ddd.demo.accounting.description.CustomerDescription;

public interface Customers {
  Customer create(CustomerDescription description, AccountSeed... accounts);

  Optional<Customer> findByIdentity(String id);

  record AccountSeed(String identity, AccountDescription description) {}
}
