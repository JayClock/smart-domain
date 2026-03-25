package reengineering.ddd.demo.accounting.memory;

import io.github.jayclock.smartdomain.mybatis.memory.Reference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.description.AccountDescription;
import reengineering.ddd.demo.accounting.description.CustomerDescription;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.description.SourceEvidenceDescription;
import reengineering.ddd.demo.accounting.description.TransactionDescription;
import reengineering.ddd.demo.accounting.model.Account;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Customers;
import reengineering.ddd.demo.accounting.model.SalesSettlement;
import reengineering.ddd.demo.accounting.model.SourceEvidence;
import reengineering.ddd.demo.accounting.model.Transaction;
import reengineering.ddd.demo.accounting.mybatis.AccountTransactions;
import reengineering.ddd.demo.accounting.mybatis.AccountingLedgerMapper;

@Component
public class InMemoryCustomers implements Customers, AccountingLedgerMapper {
  private final Map<String, CustomerRecord> customers = new LinkedHashMap<>();
  private final Map<String, AccountRecord> accounts = new LinkedHashMap<>();
  private final Map<String, EvidenceRecord> evidences = new LinkedHashMap<>();
  private final Map<String, TransactionRecord> transactions = new LinkedHashMap<>();

  private int nextCustomerId = 1;
  private int nextEvidenceId = 1;
  private int nextTransactionId = 1;

  @Override
  public Customer create(CustomerDescription description, AccountSeed... seeds) {
    String customerId = String.valueOf(nextCustomerId++);
    customers.put(customerId, new CustomerRecord(customerId, description));
    for (AccountSeed seed : seeds) {
      accounts.put(
          seed.identity(), new AccountRecord(seed.identity(), customerId, seed.description()));
    }
    return rehydrateCustomer(customerId);
  }

  @Override
  public Optional<Customer> findByIdentity(String id) {
    return Optional.ofNullable(customers.get(id))
        .map(record -> rehydrateCustomer(record.identity()));
  }

  public List<Account> accountsOf(String customerId) {
    return accounts.values().stream()
        .filter(record -> record.customerId().equals(customerId))
        .map(record -> rehydrateAccount(record.identity()))
        .toList();
  }

  public Optional<Account> findAccount(String customerId, String accountId) {
    AccountRecord record = accounts.get(accountId);
    if (record == null || !record.customerId().equals(customerId)) {
      return Optional.empty();
    }
    return Optional.of(rehydrateAccount(accountId));
  }

  public Optional<Customer> findCustomerByAccount(String accountId) {
    AccountRecord record = accounts.get(accountId);
    if (record == null) {
      return Optional.empty();
    }
    return Optional.of(rehydrateCustomer(record.customerId()));
  }

  public void updateAccount(String customerId, String accountId, Account.AccountChange change) {
    AccountRecord record = accounts.get(accountId);
    if (record == null || !record.customerId().equals(customerId)) {
      return;
    }
    accounts.put(
        accountId,
        new AccountRecord(
            record.identity(),
            record.customerId(),
            new AccountDescription(
                reengineering.ddd.demo.accounting.description.basic.Amount.sum(
                    record.description().current(), change.total()))));
  }

  public List<SourceEvidence<?>> sourceEvidencesOf(String customerId) {
    return evidences.values().stream()
        .filter(record -> record.customerId().equals(customerId))
        .<SourceEvidence<?>>map(record -> rehydrateEvidence(record.identity()))
        .toList();
  }

  public Optional<SourceEvidence<?>> findSourceEvidence(String customerId, String evidenceId) {
    EvidenceRecord record = evidences.get(evidenceId);
    if (record == null || !record.customerId().equals(customerId)) {
      return Optional.empty();
    }
    return Optional.of(rehydrateEvidence(evidenceId));
  }

  public Optional<Customer> findCustomerBySourceEvidence(String evidenceId) {
    EvidenceRecord record = evidences.get(evidenceId);
    if (record == null) {
      return Optional.empty();
    }
    return Optional.of(rehydrateCustomer(record.customerId()));
  }

  public SourceEvidence<?> createSourceEvidence(
      String customerId, SourceEvidenceDescription description) {
    String evidenceId = String.valueOf(nextEvidenceId++);
    evidences.put(evidenceId, new EvidenceRecord(evidenceId, customerId, description));
    return rehydrateEvidence(evidenceId);
  }

  @Override
  public List<Transaction> findTransactionsByAccountId(String accountId, int from, int limit) {
    return transactions.values().stream()
        .filter(record -> record.accountId().equals(accountId))
        .skip(from)
        .limit(limit)
        .map(record -> rehydrateTransaction(record.identity()))
        .toList();
  }

  @Override
  public Transaction findTransactionByAccountAndId(String accountId, String transactionId) {
    TransactionRecord record = transactions.get(transactionId);
    if (record == null || !record.accountId().equals(accountId)) {
      return null;
    }
    return rehydrateTransaction(record.identity());
  }

  @Override
  public int countTransactionsInAccount(String accountId) {
    return (int)
        transactions.values().stream()
            .filter(record -> record.accountId().equals(accountId))
            .count();
  }

  @Override
  public Transaction insertTransaction(
      String accountId, String evidenceId, TransactionDescription description) {
    String transactionId = "TX-" + String.format("%03d", nextTransactionId++);
    transactions.put(
        transactionId, new TransactionRecord(transactionId, accountId, evidenceId, description));
    return rehydrateTransaction(transactionId);
  }

  @Override
  public List<Transaction> findTransactionsBySourceEvidenceId(String evidenceId) {
    return transactions.values().stream()
        .filter(record -> record.evidenceId().equals(evidenceId))
        .map(record -> rehydrateTransaction(record.identity()))
        .toList();
  }

  @Override
  public Transaction findTransactionByEvidenceAndId(String evidenceId, String transactionId) {
    TransactionRecord record = transactions.get(transactionId);
    if (record == null || !record.evidenceId().equals(evidenceId)) {
      return null;
    }
    return rehydrateTransaction(record.identity());
  }

  @Override
  public int countTransactionsBySourceEvidence(String evidenceId) {
    return (int)
        transactions.values().stream()
            .filter(record -> record.evidenceId().equals(evidenceId))
            .count();
  }

  private Customer rehydrateCustomer(String customerId) {
    CustomerRecord record = customers.get(customerId);
    return new Customer(
        record.identity(),
        record.description(),
        new CustomerSourceEvidences(this, record.identity()),
        new CustomerAccounts(this, record.identity()));
  }

  private Account rehydrateAccount(String accountId) {
    AccountRecord record = accounts.get(accountId);
    return new Account(
        record.identity(), record.description(), new AccountTransactions(this, record.identity()));
  }

  private SourceEvidence<?> rehydrateEvidence(String evidenceId) {
    EvidenceRecord record = evidences.get(evidenceId);
    if (record.description() instanceof SalesSettlementDescription description) {
      return new SalesSettlement(
          record.identity(), description, new SourceEvidenceTransactions(this, record.identity()));
    }
    throw new IllegalArgumentException(
        "Unsupported source evidence: " + record.description().getClass());
  }

  private Transaction rehydrateTransaction(String transactionId) {
    TransactionRecord record = transactions.get(transactionId);
    return new Transaction(
        record.identity(),
        record.description(),
        new Reference<>(rehydrateAccount(record.accountId())),
        new Reference<>(rehydrateEvidence(record.evidenceId())));
  }

  private record CustomerRecord(String identity, CustomerDescription description) {}

  private record AccountRecord(
      String identity, String customerId, AccountDescription description) {}

  private record EvidenceRecord(
      String identity, String customerId, SourceEvidenceDescription description) {}

  private record TransactionRecord(
      String identity, String accountId, String evidenceId, TransactionDescription description) {}

  private static final class CustomerAccounts extends MemoryAssociation<String, Account>
      implements Customer.Accounts {
    private final InMemoryCustomers store;
    private final String customerId;

    private CustomerAccounts(InMemoryCustomers store, String customerId) {
      this.store = store;
      this.customerId = customerId;
    }

    @Override
    protected List<Account> snapshot() {
      return new ArrayList<>(store.accountsOf(customerId));
    }

    @Override
    public void update(Account account, Account.AccountChange change) {
      store.updateAccount(customerId, account.getIdentity(), change);
    }
  }

  private static final class CustomerSourceEvidences
      extends MemoryAssociation<String, SourceEvidence<?>> implements Customer.SourceEvidences {
    private final InMemoryCustomers store;
    private final String customerId;

    private CustomerSourceEvidences(InMemoryCustomers store, String customerId) {
      this.store = store;
      this.customerId = customerId;
    }

    @Override
    protected List<SourceEvidence<?>> snapshot() {
      return new ArrayList<>(store.sourceEvidencesOf(customerId));
    }

    @Override
    public SourceEvidence<?> add(SourceEvidenceDescription description) {
      return store.createSourceEvidence(customerId, description);
    }
  }
}
