package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.context.ContextRole;

public interface Auditor extends ContextRole<Operator, Customer> {
  default Customer customer() {
    return context();
  }

  default HasMany<String, Account> accounts() {
    return customer().accounts();
  }

  default Account account(String accountId) {
    return customer()
        .accounts()
        .findByIdentity(accountId)
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
  }

  default HasMany<String, Transaction> transactions(String accountId) {
    return account(accountId).transactions();
  }
}
