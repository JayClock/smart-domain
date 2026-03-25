package reengineering.ddd.demo.ecommerce.api;

import io.github.jayclock.smartdomain.tool.apimodeltree.ApiModelNode;
import io.github.jayclock.smartdomain.tool.apimodeltree.ApiModelTreeOptions;
import io.github.jayclock.smartdomain.tool.apimodeltree.SmartDomainTools;
import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
@Path("ecommerce")
public class EcommerceApi {
  private final EcommerceDemoFacade facade;

  public EcommerceApi(EcommerceDemoFacade facade) {
    this.facade = facade;
  }

  @GET
  @Path("agent-tree")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiModelNode agentTree(@DefaultValue("false") @QueryParam("includeCycle") boolean includeCycle) {
    return materialize(
        SmartDomainTools.apiModelTree(
            EcommerceRootModel.class, new ApiModelTreeOptions(includeCycle)));
  }

  @GET
  @VendorMediaType(EcommerceMediaTypes.ROOT)
  public EcommerceRootModel root() {
    return EcommerceRootModel.of(
        facade.user(), facade.buyerAccount().getIdentity(), facade.sellerStore().getIdentity());
  }

  @GET
  @Path("users/{userId}")
  @VendorMediaType(EcommerceMediaTypes.USER)
  public UserModel user(@PathParam("userId") String userId) {
    requireSame(userId, facade.user().getIdentity());
    return UserModel.of(facade.user(), EcommerceApiTemplates.user(userId).build().getPath());
  }

  @GET
  @Path("buyer-accounts/{accountId}")
  @VendorMediaType(EcommerceMediaTypes.BUYER_ACCOUNT)
  public BuyerAccountModel buyerAccount(@PathParam("accountId") String accountId) {
    requireSame(accountId, facade.buyerAccount().getIdentity());
    return BuyerAccountModel.of(facade.buyerAccount());
  }

  @POST
  @Path("buyer-accounts/{accountId}/purchases")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(EcommerceMediaTypes.PURCHASE)
  public Response createPurchase(
      @PathParam("accountId") String accountId,
      CreatePurchaseRequest request) {
    requireSame(accountId, facade.buyerAccount().getIdentity());
    var created = facade.buy(request.productName(), request.quantity());
    String href = EcommerceApiTemplates.purchase(accountId, created.getIdentity()).build().getPath();
    return Response.created(EcommerceApiTemplates.purchase(accountId, created.getIdentity()).build())
        .entity(PurchaseModel.of(created, href))
        .build();
  }

  @GET
  @Path("buyer-accounts/{accountId}/purchases/{purchaseId}")
  @VendorMediaType(EcommerceMediaTypes.PURCHASE)
  public PurchaseModel purchase(
      @PathParam("accountId") String accountId, @PathParam("purchaseId") String purchaseId) {
    requireSame(accountId, facade.buyerAccount().getIdentity());
    var purchase =
        facade
            .buyerAccount()
            .purchases()
            .findByIdentity(purchaseId)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    return PurchaseModel.of(purchase, EcommerceApiTemplates.purchase(accountId, purchaseId).build().getPath());
  }

  @GET
  @Path("seller-stores/{storeId}")
  @VendorMediaType(EcommerceMediaTypes.SELLER_STORE)
  public SellerStoreModel sellerStore(@PathParam("storeId") String storeId) {
    requireSame(storeId, facade.sellerStore().getIdentity());
    return SellerStoreModel.of(facade.sellerStore());
  }

  @POST
  @Path("seller-stores/{storeId}/listings")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(EcommerceMediaTypes.LISTING)
  public Response createListing(
      @PathParam("storeId") String storeId,
      CreateListingRequest request) {
    requireSame(storeId, facade.sellerStore().getIdentity());
    var created = facade.sell(request.productName(), request.inventory(), request.unitPrice());
    String href = EcommerceApiTemplates.listing(storeId, created.getIdentity()).build().getPath();
    return Response.created(EcommerceApiTemplates.listing(storeId, created.getIdentity()).build())
        .entity(ListingModel.of(created, href))
        .build();
  }

  @GET
  @Path("seller-stores/{storeId}/listings/{listingId}")
  @VendorMediaType(EcommerceMediaTypes.LISTING)
  public ListingModel listing(
      @PathParam("storeId") String storeId, @PathParam("listingId") String listingId) {
    requireSame(storeId, facade.sellerStore().getIdentity());
    var listing =
        facade
            .sellerStore()
            .listings()
            .findByIdentity(listingId)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    return ListingModel.of(listing, EcommerceApiTemplates.listing(storeId, listingId).build().getPath());
  }

  private void requireSame(String requestedId, String actualId) {
    if (!actualId.equals(requestedId)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  private ApiModelNode materialize(ApiModelNode node) {
    String api = node.api();
    if (api != null) {
      api =
          api.replace("{userId}", facade.user().getIdentity())
              .replace("{accountId}", facade.buyerAccount().getIdentity())
              .replace("{storeId}", facade.sellerStore().getIdentity());
    }
    return new ApiModelNode(
        node.rel(), api, node.cycle(), node.links().stream().map(this::materialize).toList());
  }

  public record CreatePurchaseRequest(String productName, int quantity) {}

  public record CreateListingRequest(String productName, int inventory, int unitPrice) {}
}
