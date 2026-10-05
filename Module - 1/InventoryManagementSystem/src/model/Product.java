package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an item in the inventory system.
 */
public class Product {
    private String id;
    private String name;
    private String sku;
    private Category category;
    private double costPrice;
    private double sellingPrice;
    private int quantity;
    private int reorderLevel;
    private String supplier;
    private LocalDateTime lastUpdated;

    public Product(String id, String name, String sku, Category category, double costPrice, double sellingPrice, int quantity, int reorderLevel, String supplier) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; touch(); }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; touch(); }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; touch(); }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; touch(); }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; touch(); }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; touch(); }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; touch(); }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; touch(); }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    public double getTotalCostValue() {
        return quantity * costPrice;
    }

    public double getTotalRetailValue() {
        return quantity * sellingPrice;
    }

    public double getExpectedProfit() {
        return (sellingPrice - costPrice) * quantity;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public String toCsvRow() {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join(",",
                id,
                escapeCsv(name),
                sku,
                category.name(),
                String.valueOf(costPrice),
                String.valueOf(sellingPrice),
                String.valueOf(quantity),
                String.valueOf(reorderLevel),
                escapeCsv(supplier),
                lastUpdated != null ? lastUpdated.format(dtf) : LocalDateTime.now().format(dtf)
        );
    }

    public static Product fromCsvRow(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) return null;
        String id = parts[0].trim();
        String name = parts[1].trim();
        String sku = parts[2].trim();
        Category cat = Category.valueOf(parts[3].trim().toUpperCase());
        double cost = Double.parseDouble(parts[4].trim());
        double sell = Double.parseDouble(parts[5].trim());
        int qty = Integer.parseInt(parts[6].trim());
        int reorder = Integer.parseInt(parts[7].trim());
        String supplier = parts[8].trim();
        Product product = new Product(id, name, sku, cat, cost, sell, qty, reorder, supplier);
        if (parts.length >= 10 && !parts[9].trim().isEmpty()) {
            try {
                product.setLastUpdated(LocalDateTime.parse(parts[9].trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception ignored) {}
        }
        return product;
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace(",", " ");
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | SKU: %-8s | Cat: %-12s | Qty: %3d | Buy: $%6.2f | Sell: $%6.2f | %s",
                id, name, sku, category, quantity, costPrice, sellingPrice,
                isLowStock() ? "[LOW STOCK ALERT]" : "OK");
    }
}
