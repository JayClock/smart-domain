package reengineering.ddd.demo.ecommerce.api;

public final class EcommerceMediaTypes {
  public static final String ROOT = "application/vnd.smartdomain.ecommerce.root+json";
  public static final String USER = "application/vnd.smartdomain.ecommerce.user+json";
  public static final String BUYER_ACCOUNT =
      "application/vnd.smartdomain.ecommerce.buyer-account+json";
  public static final String SELLER_STORE =
      "application/vnd.smartdomain.ecommerce.seller-store+json";
  public static final String PURCHASE = "application/vnd.smartdomain.ecommerce.purchase+json";
  public static final String LISTING = "application/vnd.smartdomain.ecommerce.listing+json";

  private EcommerceMediaTypes() {}
}
