package sample.smartdomain.api;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

public class SalesSettlementModel extends RepresentationModel<SalesSettlementModel> {
  private final String id;
  private final String orderId;
  private final String accountId;
  private final SettlementBreakdown breakdown;

  private SalesSettlementModel(
      String id, String orderId, String accountId, SettlementBreakdown breakdown) {
    this.id = id;
    this.orderId = orderId;
    this.accountId = accountId;
    this.breakdown = breakdown;
  }

  public static SalesSettlementModel of(SalesSettlementRecord settlement, String selfHref) {
    SalesSettlementModel model =
        new SalesSettlementModel(
            settlement.id(), settlement.orderId(), settlement.accountId(), settlement.breakdown());
    model.add(Link.of(selfHref).withSelfRel());
    return model;
  }

  public String getId() {
    return id;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getAccountId() {
    return accountId;
  }

  public SettlementBreakdown getBreakdown() {
    return breakdown;
  }
}
