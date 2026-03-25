package sample.smartdomain.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SalesSettlementCatalog {
  private final Map<String, SalesSettlementRecord> settlements = new LinkedHashMap<>();

  public SalesSettlementCatalog() {
    SalesSettlementRecord sample =
        new SalesSettlementRecord(
            "settlement-1",
            "ORDER-1001",
            "CASH-001",
            new SettlementBreakdown("1000.00", 2));
    settlements.put(sample.id(), sample);
  }

  public List<SalesSettlementRecord> findAll() {
    return new ArrayList<>(settlements.values());
  }

  public SalesSettlementRecord findById(String id) {
    return settlements.get(id);
  }

  public SalesSettlementRecord create(
      String orderId, String accountId, SettlementBreakdown breakdown) {
    String id = "settlement-" + (settlements.size() + 1);
    SalesSettlementRecord created = new SalesSettlementRecord(id, orderId, accountId, breakdown);
    settlements.put(id, created);
    return created;
  }
}
