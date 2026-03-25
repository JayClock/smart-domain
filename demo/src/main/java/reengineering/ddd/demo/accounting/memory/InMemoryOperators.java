package reengineering.ddd.demo.accounting.memory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.description.OperatorDescription;
import reengineering.ddd.demo.accounting.model.Operator;
import reengineering.ddd.demo.accounting.model.Operators;

@Component
public class InMemoryOperators implements Operators {
  private final Map<String, Operator> operators = new LinkedHashMap<>();
  private int nextId = 1;

  @Override
  public Operator create(OperatorDescription description) {
    String identity = String.valueOf(nextId++);
    Operator operator = new Operator(identity, description);
    operators.put(identity, operator);
    return operator;
  }

  @Override
  public Optional<Operator> findByIdentity(String identity) {
    return Optional.ofNullable(operators.get(identity));
  }
}
