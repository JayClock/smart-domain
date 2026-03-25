package reengineering.ddd.demo.accounting.memory;

import java.util.List;
import reengineering.ddd.demo.accounting.model.SourceEvidence;
import reengineering.ddd.demo.accounting.model.Transaction;
import reengineering.ddd.demo.accounting.mybatis.AccountingLedgerMapper;

public class SourceEvidenceTransactions extends MemoryAssociation<String, Transaction>
    implements SourceEvidence.Transactions {
  private final AccountingLedgerMapper mapper;
  private final String evidenceId;

  public SourceEvidenceTransactions(AccountingLedgerMapper mapper, String evidenceId) {
    this.mapper = mapper;
    this.evidenceId = evidenceId;
  }

  @Override
  protected List<Transaction> snapshot() {
    return mapper.findTransactionsBySourceEvidenceId(evidenceId);
  }
}
