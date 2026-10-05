package report;

import config.ProcessorConfig;
import model.ProcessingSummary;
import model.SalesTransaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates results across all worker threads and computes multi-dimensional financial & sales metrics.
 */
public class AnalyticsReportAggregator {

    private final List<SalesTransaction> allTransactions = Collections.synchronizedList(new ArrayList<>());
    private final List<ProcessingSummary> threadSummaries = Collections.synchronizedList(new ArrayList<>());

    public void addSummary(ProcessingSummary summary) {
        threadSummaries.add(summary);
        allTransactions.addAll(summary.getValidRecords());
    }

    public void generateReports(ProcessorConfig config, long totalExecutionTimeMs) {
        String reportText = buildTextReport(config, totalExecutionTimeMs);

        // 1. Output to Console
        System.out.println(reportText);

        // 2. Export Text Report File
        try (PrintWriter pw = new PrintWriter(new FileWriter(config.getReportFile()))) {
            pw.print(reportText);
            System.out.println("[EXPORT] Full Analytics Report saved to: " + config.getReportFile());
        } catch (IOException e) {
            System.err.println("[ERROR] Failed writing text report: " + e.getMessage());
        }

        // 3. Export Category Summary CSV File
        exportCategoryCsv(config.getCategoryCsvExport());
    }

    private String buildTextReport(ProcessorConfig config, long totalExecutionTimeMs) {
        StringBuilder sb = new StringBuilder();
        String banner = "=".repeat(80);
        String subBanner = "-".repeat(80);

        int totalFiles = threadSummaries.size();
        int totalLines = threadSummaries.stream().mapToInt(ProcessingSummary::getLinesRead).sum();
        int totalValid = allTransactions.size();
        int totalErrors = threadSummaries.stream().mapToInt(ProcessingSummary::getMalformedLines).sum();
        int totalUnits = allTransactions.stream().mapToInt(SalesTransaction::getUnitsSold).sum();
        double grossRevenue = allTransactions.stream().mapToDouble(SalesTransaction::getGrossAmount).sum();
        double totalDiscounts = allTransactions.stream().mapToDouble(SalesTransaction::getDiscountAmount).sum();
        double netRevenue = allTransactions.stream().mapToDouble(SalesTransaction::getNetAmount).sum();
        double totalTax = allTransactions.stream().mapToDouble(SalesTransaction::getTaxAmount).sum();
        double averageOrderValue = totalValid > 0 ? netRevenue / totalValid : 0;

        sb.append("\n").append(banner).append("\n");
        sb.append("      ENTERPRISE MULTI-THREADED SALES & REVENUE ANALYTICS REPORT\n");
        sb.append("      Processed At: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append(banner).append("\n\n");

        sb.append(">>> CONCURRENCY & THREAD WORKLOAD BENCHMARK <<<\n");
        sb.append(String.format("  Worker Thread Count  : %d threads\n", config.getThreadCount()));
        sb.append(String.format("  Files Processed      : %d files\n", totalFiles));
        sb.append(String.format("  Total Input Lines    : %,d lines\n", totalLines));
        sb.append(String.format("  Valid Transactions   : %,d records\n", totalValid));
        sb.append(String.format("  Malformed/Skipped    : %,d records\n", totalErrors));
        sb.append(String.format("  Total Benchmark Time : %d ms\n\n", totalExecutionTimeMs));

        sb.append(subBanner).append("\n");
        sb.append("  THREAD WORKLOAD PER-FILE BREAKDOWN:\n");
        sb.append(subBanner).append("\n");
        for (ProcessingSummary s : threadSummaries) {
            sb.append(String.format("  • [%-18s] %-25s | Lines: %3d | Valid: %3d | Net: $%,10.2f | Time: %2d ms\n",
                    s.getThreadName(),
                    new java.io.File(s.getFilePath()).getName(),
                    s.getLinesRead(),
                    s.getValidRecords().size(),
                    s.getTotalNetRevenue(),
                    s.getExecutionTimeMs()));
        }
        sb.append("\n");

        sb.append(subBanner).append("\n");
        sb.append(">>> EXECUTIVE FINANCIAL SUMMARY <<<\n");
        sb.append(subBanner).append("\n");
        sb.append(String.format("  Total Units Sold          : %,d units\n", totalUnits));
        sb.append(String.format("  Gross Sales Volume        : $%,14.2f\n", grossRevenue));
        sb.append(String.format("  Total Discounts Applied   : $%,14.2f (%.1f%% avg)\n",
                totalDiscounts, grossRevenue > 0 ? (totalDiscounts / grossRevenue) * 100 : 0));
        sb.append(String.format("  Net Revenue Realized      : $%,14.2f\n", netRevenue));
        sb.append(String.format("  Estimated Tax (8%%)        : $%,14.2f\n", totalTax));
        sb.append(String.format("  Total Billed Value        : $%,14.2f\n", netRevenue + totalTax));
        sb.append(String.format("  Average Order Value (AOV) : $%,14.2f\n\n", averageOrderValue));

        // Category Breakdown
        sb.append(subBanner).append("\n");
        sb.append(">>> REVENUE & UNITS BY CATEGORY <<<\n");
        sb.append(subBanner).append("\n");
        sb.append(String.format("  %-22s | %-12s | %-16s | %-12s\n", "Category", "Units Sold", "Net Revenue", "Revenue %"));
        sb.append(subBanner).append("\n");

        Map<String, Double> categoryRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getCategory, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        Map<String, Integer> categoryUnits = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getCategory, Collectors.summingInt(SalesTransaction::getUnitsSold)));

        categoryRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    String cat = entry.getKey();
                    double rev = entry.getValue();
                    int units = categoryUnits.getOrDefault(cat, 0);
                    double share = netRevenue > 0 ? (rev / netRevenue) * 100 : 0;
                    sb.append(String.format("  %-22s | %,12d | $%,15.2f | %10.1f%%\n", cat, units, rev, share));
                });
        sb.append("\n");

        // Regional Breakdown
        sb.append(subBanner).append("\n");
        sb.append(">>> REGIONAL MARKET BREAKDOWN <<<\n");
        sb.append(subBanner).append("\n");
        Map<String, Double> regionRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getRegion, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        regionRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("  %-22s : $%,14.2f (%.1f%%)\n",
                        entry.getKey(), entry.getValue(), netRevenue > 0 ? (entry.getValue() / netRevenue) * 100 : 0)));
        sb.append("\n");

        // Payment Methods Breakdown
        sb.append(subBanner).append("\n");
        sb.append(">>> PAYMENT METHOD DISTRIBUTION <<<\n");
        sb.append(subBanner).append("\n");
        Map<String, Double> paymentRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getPaymentMethod, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        paymentRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("  %-22s : $%,14.2f\n", entry.getKey(), entry.getValue())));
        sb.append("\n");

        // Customer Type Breakdown
        sb.append(subBanner).append("\n");
        sb.append(">>> CUSTOMER SEGMENTATION <<<\n");
        sb.append(subBanner).append("\n");
        Map<String, Double> customerRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getCustomerType, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        customerRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("  %-22s : $%,14.2f\n", entry.getKey(), entry.getValue())));
        sb.append("\n");

        // Top 5 Products by Revenue
        sb.append(subBanner).append("\n");
        sb.append(">>> TOP 5 PRODUCTS (BY NET REVENUE) <<<\n");
        sb.append(subBanner).append("\n");
        Map<String, Double> productRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getProduct, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        productRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> sb.append(String.format("  ★ %-30s : $%,12.2f\n", entry.getKey(), entry.getValue())));

        sb.append(banner).append("\n");
        return sb.toString();
    }

    private void exportCategoryCsv(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return;

        Map<String, Double> categoryRevenue = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getCategory, Collectors.summingDouble(SalesTransaction::getNetAmount)));
        Map<String, Integer> categoryUnits = allTransactions.stream()
                .collect(Collectors.groupingBy(SalesTransaction::getCategory, Collectors.summingInt(SalesTransaction::getUnitsSold)));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Category,UnitsSold,NetRevenue,AverageUnitPrice");
            writer.newLine();

            for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                String cat = entry.getKey();
                double rev = entry.getValue();
                int units = categoryUnits.getOrDefault(cat, 0);
                double avgPrice = units > 0 ? rev / units : 0;

                writer.write(String.format("%s,%d,%.2f,%.2f", cat, units, rev, avgPrice));
                writer.newLine();
            }
            System.out.println("[EXPORT] Category summary CSV exported to: " + filePath);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to export category summary CSV: " + e.getMessage());
        }
    }
}
