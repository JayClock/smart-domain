package reengineering.ddd.demo.accounting.api;

import io.github.jayclock.smartdomain.api.hateoas.media.VendorMediaType;
import io.github.jayclock.smartdomain.tool.apimodeltree.ApiModelNode;
import io.github.jayclock.smartdomain.tool.apimodeltree.ApiModelTreeOptions;
import io.github.jayclock.smartdomain.tool.apimodeltree.SmartDomainTools;
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
import java.util.List;
import org.springframework.stereotype.Component;
import reengineering.ddd.demo.accounting.description.basic.Amount;
import reengineering.ddd.demo.accounting.model.SourceEvidence;

@Component
@Path("accounting")
public class AccountingApi {
  private final AccountingDemoFacade facade;

  public AccountingApi(AccountingDemoFacade facade) {
    this.facade = facade;
  }

  @GET
  @Path("agent-tree")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiModelNode agentTree(
      @DefaultValue("false") @QueryParam("includeCycle") boolean includeCycle) {
    return materialize(
        SmartDomainTools.apiModelTree(
            AccountingRootModel.class, new ApiModelTreeOptions(includeCycle)));
  }

  @GET
  @VendorMediaType(AccountingMediaTypes.ROOT)
  public AccountingRootModel root() {
    return AccountingRootModel.of(facade.operator(), facade.customer(), facade.activeRoles());
  }

  @GET
  @Path("operators/{operatorId}")
  @VendorMediaType(AccountingMediaTypes.OPERATOR)
  public OperatorModel operator(@PathParam("operatorId") String operatorId) {
    requireSame(operatorId, facade.operator().getIdentity());
    return OperatorModel.of(facade.operator(), facade.customer().getIdentity());
  }

  @GET
  @Path("customers/{customerId}")
  @VendorMediaType(AccountingMediaTypes.CUSTOMER)
  public CustomerModel customer(@PathParam("customerId") String customerId) {
    requireSame(customerId, facade.customer().getIdentity());
    return CustomerModel.of(facade.customer());
  }

  @POST
  @Path("customers/{customerId}/source-evidences/sales-settlements")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(AccountingMediaTypes.SOURCE_EVIDENCE)
  public Response createSalesSettlement(
      @PathParam("customerId") String customerId, CreateSalesSettlementRequest request) {
    requireSame(customerId, facade.customer().getIdentity());
    SourceEvidence<?> created =
        facade.recordSalesSettlement(
            request.orderId(),
            request.accountId(),
            request.detailAmounts().stream().map(Amount::cny).toList());
    return Response.created(
            AccountingApiTemplates.sourceEvidence(customerId, created.getIdentity()).build())
        .entity(SourceEvidenceModel.of(customerId, created))
        .build();
  }

  @GET
  @Path("customers/{customerId}/source-evidences/{evidenceId}")
  @VendorMediaType(AccountingMediaTypes.SOURCE_EVIDENCE)
  public SourceEvidenceModel sourceEvidence(
      @PathParam("customerId") String customerId, @PathParam("evidenceId") String evidenceId) {
    requireSame(customerId, facade.customer().getIdentity());
    SourceEvidence<?> sourceEvidence =
        facade
            .customer()
            .sourceEvidences()
            .findByIdentity(evidenceId)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    return SourceEvidenceModel.of(customerId, sourceEvidence);
  }

  @GET
  @Path("customers/{customerId}/accounts/{accountId}")
  @VendorMediaType(AccountingMediaTypes.ACCOUNT)
  public AccountModel account(
      @PathParam("customerId") String customerId, @PathParam("accountId") String accountId) {
    requireSame(customerId, facade.customer().getIdentity());
    return AccountModel.of(customerId, facade.account(accountId));
  }

  @GET
  @Path("customers/{customerId}/accounts/{accountId}/transactions/{transactionId}")
  @VendorMediaType(AccountingMediaTypes.TRANSACTION)
  public TransactionModel transaction(
      @PathParam("customerId") String customerId,
      @PathParam("accountId") String accountId,
      @PathParam("transactionId") String transactionId) {
    requireSame(customerId, facade.customer().getIdentity());
    var transaction =
        facade
            .account(accountId)
            .transactions()
            .findByIdentity(transactionId)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    return TransactionModel.of(
        transaction,
        customerId,
        AccountingApiTemplates.transaction(customerId, accountId, transactionId).build().getPath(),
        AccountingApiTemplates.sourceEvidence(
                customerId, transaction.sourceEvidence().getIdentity())
            .build()
            .getPath());
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
          api.replace("{operatorId}", facade.operator().getIdentity())
              .replace("{customerId}", facade.customer().getIdentity())
              .replace("{accountId}", "CASH-001")
              .replace("{transactionId}", "TX-001")
              .replace("{evidenceId}", "1");
    }
    return new ApiModelNode(
        node.rel(), api, node.cycle(), node.links().stream().map(this::materialize).toList());
  }

  public record CreateSalesSettlementRequest(
      String orderId, String accountId, List<String> detailAmounts) {}
}
