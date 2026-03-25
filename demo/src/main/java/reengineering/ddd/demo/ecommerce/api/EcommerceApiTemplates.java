package reengineering.ddd.demo.ecommerce.api;

import jakarta.ws.rs.core.UriBuilder;

public final class EcommerceApiTemplates {
  private EcommerceApiTemplates() {}

  public static UriBuilder root() {
    return UriBuilder.fromPath("/api").path(EcommerceApi.class);
  }

  public static UriBuilder agentTree() {
    return root().path(EcommerceApi.class, "agentTree");
  }

  public static UriBuilder user(String userId) {
    return root().path(EcommerceApi.class, "user").resolveTemplate("userId", userId);
  }

  public static UriBuilder buyerAccount(String accountId) {
    return root().path(EcommerceApi.class, "buyerAccount").resolveTemplate("accountId", accountId);
  }

  public static UriBuilder createPurchase(String accountId) {
    return root()
        .path(EcommerceApi.class, "createPurchase")
        .resolveTemplate("accountId", accountId);
  }

  public static UriBuilder purchase(String accountId, String purchaseId) {
    return root()
        .path(EcommerceApi.class, "purchase")
        .resolveTemplate("accountId", accountId)
        .resolveTemplate("purchaseId", purchaseId);
  }

  public static UriBuilder sellerStore(String storeId) {
    return root().path(EcommerceApi.class, "sellerStore").resolveTemplate("storeId", storeId);
  }

  public static UriBuilder createListing(String storeId) {
    return root()
        .path(EcommerceApi.class, "createListing")
        .resolveTemplate("storeId", storeId);
  }

  public static UriBuilder listing(String storeId, String listingId) {
    return root()
        .path(EcommerceApi.class, "listing")
        .resolveTemplate("storeId", storeId)
        .resolveTemplate("listingId", listingId);
  }
}
