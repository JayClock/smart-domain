package reengineering.ddd.demo.accounting.description;

import java.time.LocalDateTime;
import reengineering.ddd.demo.accounting.description.basic.Amount;

public record TransactionDescription(Amount amount, LocalDateTime createdAt) {}
