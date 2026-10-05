package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockTransaction {
    public enum Type {
        STOCK_IN,
        STOCK_OUT,
        ADJUSTMENT
    }

    private String transactionId;
    private String productId;
    private String productName;
    private Type type;
    private int quantity;
    private double unitPrice;
    private LocalDateTime timestamp;
    private String note;

    public StockTransaction(String transactionId, String productId, String productName, Type type, int quantity, double unitPrice, String note) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.timestamp = LocalDateTime.now();
        this.note = note;
    }

    public String getTransactionId() { return transactionId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Type getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getNote() { return note; }

    public double getTotalValue() {
        return quantity * unitPrice;
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %-10s | %-12s | %-18s | Qty: %3d | Value: $%8.2f | Note: %s",
                timestamp.format(dtf), transactionId, type, productName, quantity, getTotalValue(), note);
    }
}
