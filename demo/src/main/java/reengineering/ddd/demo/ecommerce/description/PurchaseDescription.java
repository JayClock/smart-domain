package reengineering.ddd.demo.ecommerce.description;

public record PurchaseDescription(String productName, int quantity) {
  public PurchaseDescription {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be greater than zero");
    }
  }
}
