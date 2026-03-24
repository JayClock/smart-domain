package reengineering.ddd.demo.ecommerce.api;

import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@Path("ecommerce")
public class EcommerceApi {
  private final EcommerceDemoFacade facade;

  public EcommerceApi(EcommerceDemoFacade facade) {
    this.facade = facade;
  }

  @GET
  @VendorMediaType(EcommerceMediaTypes.ROOT)
  public EcommerceRootModel root(@Context UriInfo uriInfo) {
    EcommerceRootModel model =
        EcommerceRootModel.of(
            facade.user(), facade.buyerAccount().getIdentity(), facade.sellerStore().getIdentity());
    String baseHref = uriInfo.getAbsolutePath().toString();
    model.add(Link.of(baseHref).withSelfRel());
    model.add(Link.of(baseHref + "/users/" + facade.user().getIdentity()).withRel("user"));
    model.add(
        Link.of(baseHref + "/buyer-accounts/" + facade.buyerAccount().getIdentity())
            .withRel("buyer-account"));
    model.add(
        Link.of(baseHref + "/seller-stores/" + facade.sellerStore().getIdentity())
            .withRel("seller-store"));
    return model;
  }

  @GET
  @Path("users/{userId}")
  @VendorMediaType(EcommerceMediaTypes.USER)
  public UserModel user(@PathParam("userId") String userId, @Context UriInfo uriInfo) {
    requireSame(userId, facade.user().getIdentity());
    return UserModel.of(facade.user(), uriInfo.getAbsolutePath().toString());
  }

  @GET
  @Path("buyer-accounts/{accountId}")
  @VendorMediaType(EcommerceMediaTypes.BUYER_ACCOUNT)
  public BuyerAccountModel buyerAccount(
      @PathParam("accountId") String accountId, @Context UriInfo uriInfo) {
    requireSame(accountId, facade.buyerAccount().getIdentity());
    String selfHref = uriInfo.getAbsolutePath().toString();
    BuyerAccountModel model = BuyerAccountModel.of(facade.buyerAccount(), selfHref, selfHref);
    model.add(
        Affordances.of(Link.of(selfHref).withSelfRel())
            .afford(HttpMethod.POST)
            .withInput(CreatePurchaseRequest.class)
            .withName("create-purchase")
            .toLink());
    return model;
  }

  @POST
  @Path("buyer-accounts/{accountId}/purchases")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(EcommerceMediaTypes.PURCHASE)
  public Response createPurchase(
      @PathParam("accountId") String accountId,
      CreatePurchaseRequest request,
      @Context UriInfo uriInfo) {
    requireSame(accountId, facade.buyerAccount().getIdentity());
    var created = facade.buy(request.productName(), request.quantity());
    String href =
        uriInfo.getBaseUriBuilder()
            .path("ecommerce")
            .path("buyer-accounts")
            .path(accountId)
            .path("purchases")
            .path(created.getIdentity())
            .build()
            .toString();
    return Response.created(uriInfo.getAbsolutePathBuilder().path(created.getIdentity()).build())
        .entity(PurchaseModel.of(created, href))
        .build();
  }

  @GET
  @Path("seller-stores/{storeId}")
  @VendorMediaType(EcommerceMediaTypes.SELLER_STORE)
  public SellerStoreModel sellerStore(
      @PathParam("storeId") String storeId, @Context UriInfo uriInfo) {
    requireSame(storeId, facade.sellerStore().getIdentity());
    String selfHref = uriInfo.getAbsolutePath().toString();
    SellerStoreModel model = SellerStoreModel.of(facade.sellerStore(), selfHref, selfHref);
    model.add(
        Affordances.of(Link.of(selfHref).withSelfRel())
            .afford(HttpMethod.POST)
            .withInput(CreateListingRequest.class)
            .withName("create-listing")
            .toLink());
    return model;
  }

  @POST
  @Path("seller-stores/{storeId}/listings")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(EcommerceMediaTypes.LISTING)
  public Response createListing(
      @PathParam("storeId") String storeId,
      CreateListingRequest request,
      @Context UriInfo uriInfo) {
    requireSame(storeId, facade.sellerStore().getIdentity());
    var created = facade.sell(request.productName(), request.inventory(), request.unitPrice());
    String href =
        uriInfo.getBaseUriBuilder()
            .path("ecommerce")
            .path("seller-stores")
            .path(storeId)
            .path("listings")
            .path(created.getIdentity())
            .build()
            .toString();
    return Response.created(uriInfo.getAbsolutePathBuilder().path(created.getIdentity()).build())
        .entity(ListingModel.of(created, href))
        .build();
  }

  private void requireSame(String requestedId, String actualId) {
    if (!actualId.equals(requestedId)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  public record CreatePurchaseRequest(String productName, int quantity) {}

  public record CreateListingRequest(String productName, int inventory, int unitPrice) {}
}
