package reengineering.ddd.demo.accounting.api;

import jakarta.ws.rs.core.UriBuilder;

public final class AccountingApiTemplates {
  private AccountingApiTemplates() {}

  public static UriBuilder root() {
    return UriBuilder.fromPath("/api").path(AccountingApi.class);
  }

  public static UriBuilder agentTree() {
    return root().path(AccountingApi.class, "agentTree");
  }

  public static UriBuilder operator(String operatorId) {
    return root().path(AccountingApi.class, "operator").resolveTemplate("operatorId", operatorId);
  }

  public static UriBuilder customer(String customerId) {
    return root().path(AccountingApi.class, "customer").resolveTemplate("customerId", customerId);
  }

  public static UriBuilder createSalesSettlement(String customerId) {
    return root()
        .path(AccountingApi.class, "createSalesSettlement")
        .resolveTemplate("customerId", customerId);
  }

  public static UriBuilder sourceEvidence(String customerId, String evidenceId) {
    return root()
        .path(AccountingApi.class, "sourceEvidence")
        .resolveTemplate("customerId", customerId)
        .resolveTemplate("evidenceId", evidenceId);
  }

  public static UriBuilder account(String customerId, String accountId) {
    return root()
        .path(AccountingApi.class, "account")
        .resolveTemplate("customerId", customerId)
        .resolveTemplate("accountId", accountId);
  }

  public static UriBuilder transaction(String customerId, String accountId, String transactionId) {
    return root()
        .path(AccountingApi.class, "transaction")
        .resolveTemplate("customerId", customerId)
        .resolveTemplate("accountId", accountId)
        .resolveTemplate("transactionId", transactionId);
  }
}
