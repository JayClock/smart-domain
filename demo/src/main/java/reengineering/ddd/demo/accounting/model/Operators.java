package reengineering.ddd.demo.accounting.model;

import java.util.Optional;
import reengineering.ddd.demo.accounting.description.OperatorDescription;

public interface Operators {
  Operator create(OperatorDescription description);

  Optional<Operator> findByIdentity(String identity);
}
