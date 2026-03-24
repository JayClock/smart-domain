package reengineering.ddd.demo.ecommerce.description;

public record ListingDescription(String productName, int inventory, int unitPrice) {
  public ListingDescription {
    if (inventory <= 0) {
      throw new IllegalArgumentException("inventory must be greater than zero");
    }
    if (unitPrice < 0) {
      throw new IllegalArgumentException("unitPrice must not be negative");
    }
  }
}
