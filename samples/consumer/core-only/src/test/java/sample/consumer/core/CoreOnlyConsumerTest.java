package sample.consumer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jayclock.smartdomain.core.Entity;
import io.github.jayclock.smartdomain.core.HasMany;
import io.github.jayclock.smartdomain.core.Many;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CoreOnlyConsumerTest {

  @Test
  void should_use_core_contract_without_team_ai_runtime() {
    Transaction transaction = new Transaction("tx-1", "CNY 1280.00");
    Transactions transactions = new Transactions(List.of(transaction));

    assertEquals(1, transactions.size());
    assertEquals(Optional.of(transaction), transactions.findByIdentity("tx-1"));
    assertTrue(
        transactions.stream().map(Transaction::getDescription).toList().contains("CNY 1280.00"));
  }

  private static final class Transaction implements Entity<String, String> {
    private final String identity;
    private final String description;

    private Transaction(String identity, String description) {
      this.identity = identity;
      this.description = description;
    }

    @Override
    public String getIdentity() {
      return identity;
    }

    @Override
    public String getDescription() {
      return description;
    }
  }

  private static final class Transactions
      implements Many<Transaction>, HasMany<String, Transaction> {
    private final List<Transaction> transactions;

    private Transactions(List<Transaction> transactions) {
      this.transactions = transactions;
    }

    @Override
    public Many<Transaction> findAll() {
      return this;
    }

    @Override
    public Optional<Transaction> findByIdentity(String identifier) {
      return transactions.stream()
          .filter(transaction -> transaction.getIdentity().equals(identifier))
          .findFirst();
    }

    @Override
    public int size() {
      return transactions.size();
    }

    @Override
    public Many<Transaction> subCollection(int from, int to) {
      return new Transactions(transactions.subList(from, to));
    }

    @Override
    public Iterator<Transaction> iterator() {
      return transactions.iterator();
    }
  }
}
