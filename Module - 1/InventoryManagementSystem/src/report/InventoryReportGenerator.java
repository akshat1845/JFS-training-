package report;

import model.Category;
import model.Product;
import model.StockTransaction;
import service.InventoryService;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class InventoryReportGenerator {

    public static String generateComprehensiveReport(InventoryService service) {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(75);
        String subline = "-".repeat(75);

        sb.append(line).append("\n");
        sb.append("         ENTERPRISE INVENTORY INTELLIGENCE & AUDIT REPORT\n");
        sb.append("         Generated At: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n");
        sb.append(line).append("\n\n");

        List<Product> allProducts = service.getAllProducts();
        List<Product> lowStock = service.getLowStockProducts();
        double totalCost = service.calculateTotalInventoryCost();
        double totalRetail = service.calculateTotalInventoryRetailValue();
        double profitPotential = service.calculateTotalExpectedProfit();

        sb.append(">>> EXECUTIVE METRICS <<<\n");
        sb.append(String.format("  Total Distinct SKU Products : %d\n", allProducts.size()));
        sb.append(String.format("  Total Units In Stock        : %d units\n",
                allProducts.stream().mapToInt(Product::getQuantity).sum()));
        sb.append(String.format("  Total Asset Value (At Cost) : $%,.2f\n", totalCost));
        sb.append(String.format("  Total Retail Inventory Value: $%,.2f\n", totalRetail));
        sb.append(String.format("  Projected Gross Margin      : $%,.2f (%.1f%%)\n",
                profitPotential, totalRetail > 0 ? (profitPotential / totalRetail) * 100 : 0));
        sb.append(String.format("  Items Requiring Reorder     : %d\n\n", lowStock.size()));

        sb.append(subline).append("\n");
        sb.append(">>> CATEGORY BREAKDOWN <<<\n");
        sb.append(subline).append("\n");
        sb.append(String.format("  %-20s | %-12s | %-20s\n", "Category", "Products", "Retail Value"));
        sb.append(subline).append("\n");
        Map<Category, Long> counts = service.getProductCountByCategory();
        Map<Category, Double> values = service.getInventoryValueByCategory();
        for (Category cat : Category.values()) {
            long count = counts.getOrDefault(cat, 0L);
            double val = values.getOrDefault(cat, 0.0);
            if (count > 0) {
                sb.append(String.format("  %-20s | %-12d | $%,18.2f\n", cat.name(), count, val));
            }
        }
        sb.append("\n");

        sb.append(subline).append("\n");
        sb.append(">>> LOW STOCK ALERT MONITOR <<<\n");
        sb.append(subline).append("\n");
        if (lowStock.isEmpty()) {
            sb.append("  [OK] All stock levels are within safe operating thresholds.\n");
        } else {
            sb.append(String.format("  %-6s | %-24s | %-10s | %-10s | %-15s\n",
                    "ID", "Product Name", "Available", "Threshold", "Supplier"));
            sb.append(subline).append("\n");
            for (Product p : lowStock) {
                sb.append(String.format("  %-6s | %-24s | %-10d | %-10d | %-15s\n",
                        p.getId(),
                        p.getName().length() > 24 ? p.getName().substring(0, 21) + "..." : p.getName(),
                        p.getQuantity(),
                        p.getReorderLevel(),
                        p.getSupplier() != null ? p.getSupplier() : "N/A"));
            }
        }
        sb.append("\n");

        sb.append(subline).append("\n");
        sb.append(">>> RECENT AUDIT TRANSACTION LOG (LAST 10) <<<\n");
        sb.append(subline).append("\n");
        List<StockTransaction> txs = service.getTransactionHistory();
        if (txs.isEmpty()) {
            sb.append("  No transactions recorded.\n");
        } else {
            int start = Math.max(0, txs.size() - 10);
            for (int i = start; i < txs.size(); i++) {
                sb.append("  ").append(txs.get(i).toString()).append("\n");
            }
        }
        sb.append(line).append("\n");

        return sb.toString();
    }

    public static void saveReportToFile(InventoryService service, String filePath) throws IOException {
        String report = generateComprehensiveReport(service);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.print(report);
        }
    }
}
