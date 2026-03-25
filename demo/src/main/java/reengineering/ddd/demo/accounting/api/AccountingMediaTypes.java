package reengineering.ddd.demo.accounting.api;

public final class AccountingMediaTypes {
  public static final String ROOT = "application/vnd.smartdomain.accounting.root+json";
  public static final String OPERATOR = "application/vnd.smartdomain.accounting.operator+json";
  public static final String CUSTOMER = "application/vnd.smartdomain.accounting.customer+json";
  public static final String ACCOUNT = "application/vnd.smartdomain.accounting.account+json";
  public static final String SOURCE_EVIDENCE =
      "application/vnd.smartdomain.accounting.source-evidence+json";
  public static final String TRANSACTION =
      "application/vnd.smartdomain.accounting.transaction+json";

  private AccountingMediaTypes() {}
}
