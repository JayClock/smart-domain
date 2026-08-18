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
import reengineering.ddd.demo.accounting.bootstrap.AccountingDemoFixture;
import reengineering.ddd.demo.accounting.description.SalesSettlementDescription;
import reengineering.ddd.demo.accounting.description.basic.Amount;
import reengineering.ddd.demo.accounting.model.AuditContext;
import reengineering.ddd.demo.accounting.model.Auditor;
import reengineering.ddd.demo.accounting.model.BookkeepingContext;
import reengineering.ddd.demo.accounting.model.Customer;
import reengineering.ddd.demo.accounting.model.Customers;
import reengineering.ddd.demo.accounting.model.Operator;
import reengineering.ddd.demo.accounting.model.Operators;
import reengineering.ddd.demo.accounting.model.SourceEvidence;

@Component
@Path("accounting")
public class AccountingApi {
  private final AccountingDemoFixture fixture;
  private final Operators operators;
  private final Customers customers;
  private final BookkeepingContext bookkeepingContext;
  private final AuditContext auditContext;

  public AccountingApi(
      AccountingDemoFixture fixture,
      Operators operators,
      Customers customers,
      BookkeepingContext bookkeepingContext,
      AuditContext auditContext) {
    this.fixture = fixture;
    this.operators = operators;
    this.customers = customers;
    this.bookkeepingContext = bookkeepingContext;
    this.auditContext = auditContext;
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
    Operator operator = operator();
    Customer customer = customer();
    return AccountingRootModel.of(operator, customer, fixture.activeRoles(operator, customer));
  }

  @GET
  @Path("operators/{operatorId}")
  @VendorMediaType(AccountingMediaTypes.OPERATOR)
  public OperatorModel operator(@PathParam("operatorId") String operatorId) {
    requireSame(operatorId, fixture.operatorId());
    return OperatorModel.of(operator(), fixture.customerId());
  }

  @GET
  @Path("customers/{customerId}")
  @VendorMediaType(AccountingMediaTypes.CUSTOMER)
  public CustomerModel customer(@PathParam("customerId") String customerId) {
    requireSame(customerId, fixture.customerId());
    return CustomerModel.of(customer());
  }

  @POST
  @Path("customers/{customerId}/source-evidences/sales-settlements")
  @Consumes(MediaType.APPLICATION_JSON)
  @VendorMediaType(AccountingMediaTypes.SOURCE_EVIDENCE)
  public Response createSalesSettlement(
      @PathParam("customerId") String customerId, CreateSalesSettlementRequest request) {
    requireSame(customerId, fixture.customerId());
    List<Amount> detailAmounts = request.detailAmounts().stream().map(Amount::cny).toList();
    SalesSettlementDescription.Detail[] details =
        detailAmounts.stream()
            .map(SalesSettlementDescription.Detail::new)
            .toArray(SalesSettlementDescription.Detail[]::new);
    SourceEvidence<?> created =
        bookkeepingContext
            .require(operator(), customer())
            .record(
                SalesSettlementDescription.of(
                    request.orderId(),
                    Amount.sum(detailAmounts.toArray(Amount[]::new)),
                    request.accountId(),
                    details));
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
    requireSame(customerId, fixture.customerId());
    SourceEvidence<?> sourceEvidence =
        customer()
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
    requireSame(customerId, fixture.customerId());
    return AccountModel.of(customerId, auditor().account(accountId));
  }

  @GET
  @Path("customers/{customerId}/accounts/{accountId}/transactions/{transactionId}")
  @VendorMediaType(AccountingMediaTypes.TRANSACTION)
  public TransactionModel transaction(
      @PathParam("customerId") String customerId,
      @PathParam("accountId") String accountId,
      @PathParam("transactionId") String transactionId) {
    requireSame(customerId, fixture.customerId());
    var transaction =
        auditor()
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

  private Operator operator() {
    return operators
        .findByIdentity(fixture.operatorId())
        .orElseThrow(() -> new IllegalStateException("demo operator is not initialized"));
  }

  private Customer customer() {
    return customers
        .findByIdentity(fixture.customerId())
        .orElseThrow(() -> new IllegalStateException("demo customer is not initialized"));
  }

  private Auditor auditor() {
    return auditContext.require(operator(), customer());
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
          api.replace("{operatorId}", fixture.operatorId())
              .replace("{customerId}", fixture.customerId())
              .replace("{accountId}", fixture.cashAccountId())
              .replace("{transactionId}", fixture.firstTransactionId())
              .replace("{evidenceId}", fixture.firstEvidenceId());
    }
    return new ApiModelNode(
        node.rel(), api, node.cycle(), node.links().stream().map(this::materialize).toList());
  }

  public record CreateSalesSettlementRequest(
      String orderId, String accountId, List<String> detailAmounts) {}
}
