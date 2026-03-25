package sample.smartdomain.api;

public final class AccountingMediaTypes {
  private static final String VENDOR = "application/vnd.smart-domain-accounting-sample";

  public static final String SALES_SETTLEMENT = VENDOR + ".sales-settlement+json";
  public static final String SALES_SETTLEMENT_COLLECTION =
      VENDOR + ".sales-settlements+json";

  private AccountingMediaTypes() {}
}
