package reengineering.ddd.demo.ecommerce.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import reengineering.ddd.demo.ecommerce.model.User;

public class EcommerceRootModel extends RepresentationModel<EcommerceRootModel> {
  private final String userId;
  private final String userName;
  private final String buyerAccountId;
  private final String sellerStoreId;

  private EcommerceRootModel(
      String userId, String userName, String buyerAccountId, String sellerStoreId) {
    this.userId = userId;
    this.userName = userName;
    this.buyerAccountId = buyerAccountId;
    this.sellerStoreId = sellerStoreId;
  }

  public static EcommerceRootModel of(User user, String buyerAccountId, String sellerStoreId) {
    EcommerceRootModel model =
        new EcommerceRootModel(
        user.getIdentity(),
        user.getDescription().name(),
        buyerAccountId,
        sellerStoreId);
    model.add(Link.of(EcommerceApiTemplates.root().build().getPath()).withSelfRel());
    model.add(
        Link.of(EcommerceApiTemplates.user(user.getIdentity()).build().getPath()).withRel("user"));
    model.add(
        Link.of(EcommerceApiTemplates.buyerAccount(buyerAccountId).build().getPath())
            .withRel("buyer-account"));
    model.add(
        Link.of(EcommerceApiTemplates.sellerStore(sellerStoreId).build().getPath())
            .withRel("seller-store"));
    return model;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getBuyerAccountId() {
    return buyerAccountId;
  }

  public String getSellerStoreId() {
    return sellerStoreId;
  }
}
