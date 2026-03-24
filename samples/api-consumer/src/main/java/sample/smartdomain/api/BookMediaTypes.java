package sample.smartdomain.api;

public final class BookMediaTypes {
  private static final String VENDOR = "application/vnd.smart-domain-sample";

  public static final String BOOK = VENDOR + ".book+json";
  public static final String BOOK_COLLECTION = VENDOR + ".books+json";

  private BookMediaTypes() {}
}
