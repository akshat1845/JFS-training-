package model;

import java.time.LocalDate;

/**
 * Domain model representing a sales and revenue transaction row.
 */
public class SalesTransaction {
    private String transactionId;
    private LocalDate date;
    private String region;
    private String customerType; // Retail, Wholesale, Enterprise
    private String category;
    private String product;
    private int unitsSold;
    private double unitPrice;
    private double discountRate; // e.g. 0.05 for 5%
    private String paymentMethod; // Credit Card, UPI, PayPal, Bank Transfer

    public SalesTransaction(String transactionId, LocalDate date, String region, String customerType,
                            String category, String product, int unitsSold, double unitPrice,
                            double discountRate, String paymentMethod) {
        this.transactionId = transactionId;
        this.date = date;
        this.region = region;
        this.customerType = customerType;
        this.category = category;
        this.product = product;
        this.unitsSold = unitsSold;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() { return transactionId; }
    public LocalDate getDate() { return date; }
    public String getRegion() { return region; }
    public String getCustomerType() { return customerType; }
    public String getCategory() { return category; }
    public String getProduct() { return product; }
    public int getUnitsSold() { return unitsSold; }
    public double getUnitPrice() { return unitPrice; }
    public double getDiscountRate() { return discountRate; }
    public String getPaymentMethod() { return paymentMethod; }

    public double getGrossAmount() {
        return unitsSold * unitPrice;
    }

    public double getDiscountAmount() {
        return getGrossAmount() * discountRate;
    }

    public double getNetAmount() {
        return getGrossAmount() - getDiscountAmount();
    }

    public double getTaxAmount() {
        return getNetAmount() * 0.08; // 8% standard tax
    }

    public double getTotalAmount() {
        return getNetAmount() + getTaxAmount();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %-12s | %-15s | %-20s | Qty: %3d | Price: $%.2f | Net: $%,.2f (%s)",
                transactionId, date, region, category, product, unitsSold, unitPrice, getNetAmount(), paymentMethod);
    }
}
