package sample.smartdomain.api;

import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import io.github.jayclock.smartdomain.api.hateoas.schema.WithJsonSchema;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@Path("sales-settlements")
public class SalesSettlementsApi {
  private final SalesSettlementCatalog settlementCatalog;

  public SalesSettlementsApi(SalesSettlementCatalog settlementCatalog) {
    this.settlementCatalog = settlementCatalog;
  }

  @GET
  @VendorMediaType(AccountingMediaTypes.SALES_SETTLEMENT_COLLECTION)
  public CollectionModel<SalesSettlementModel> findAll(@Context UriInfo uriInfo) {
    Link self = Link.of(uriInfo.getAbsolutePath().toString()).withSelfRel();
    CollectionModel<SalesSettlementModel> model =
        CollectionModel.of(
            settlementCatalog.findAll().stream()
                .map(settlement -> SalesSettlementModel.of(settlement, settlementHref(uriInfo, settlement.id())))
                .toList(),
            self);
    model.add(
        Affordances.of(self)
            .afford(HttpMethod.POST)
            .withInput(CreateSalesSettlementRequest.class)
            .andAfford(HttpMethod.POST)
            .withInput(CreateSalesSettlementRequest.class)
            .withName("record-sales-settlement")
            .toLink());
    return model;
  }

  @GET
  @Path("{id}")
  @VendorMediaType(AccountingMediaTypes.SALES_SETTLEMENT)
  public SalesSettlementModel findById(@PathParam("id") String id, @Context UriInfo uriInfo) {
    SalesSettlementRecord settlement = settlementCatalog.findById(id);
    if (settlement == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return SalesSettlementModel.of(settlement, settlementHref(uriInfo, settlement.id()));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(AccountingMediaTypes.SALES_SETTLEMENT)
  public Response create(CreateSalesSettlementRequest request, @Context UriInfo uriInfo) {
    SalesSettlementRecord created =
        settlementCatalog.create(request.orderId(), request.accountId(), request.breakdown());
    return Response.created(uriInfo.getAbsolutePathBuilder().path(created.id()).build())
        .entity(SalesSettlementModel.of(created, settlementHref(uriInfo, created.id())))
        .build();
  }

  private String settlementHref(UriInfo uriInfo, String id) {
    return uriInfo.getBaseUriBuilder().path("sales-settlements").path(id).build().toString();
  }

  public record CreateSalesSettlementRequest(
      String orderId,
      String accountId,
      @WithJsonSchema(SettlementBreakdown.class) SettlementBreakdown breakdown) {}
}
