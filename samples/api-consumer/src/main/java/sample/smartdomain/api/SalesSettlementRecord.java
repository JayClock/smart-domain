package sample.smartdomain.api;

public record SalesSettlementRecord(
    String id, String orderId, String accountId, SettlementBreakdown breakdown) {}
