package processor;

import config.ProcessorConfig;
import model.ProcessingSummary;
import model.SalesTransaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

/**
 * Callable task assigned to worker threads for parsing and verifying individual CSV files.
 */
public class CsvFileProcessor implements Callable<ProcessingSummary> {
    private final String filePath;
    private final ProcessorConfig config;

    public CsvFileProcessor(String filePath, ProcessorConfig config) {
        this.filePath = filePath;
        this.config = config;
    }

    @Override
    public ProcessingSummary call() throws Exception {
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        ProcessingSummary summary = new ProcessingSummary(filePath, threadName);

        if (config.isVerboseLogging()) {
            System.out.println(String.format(" -> [%s] Started parsing: %s", threadName, filePath));
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                summary.incrementLinesRead();

                if (isHeader && config.isSkipHeader()) {
                    isHeader = false;
                    continue;
                }
                isHeader = false;

                line = line.trim();
                if (line.isEmpty()) continue;

                SalesTransaction record = parseTransactionLine(line);
                if (record != null) {
                    if (record.getNetAmount() >= config.getMinTransactionFilter()) {
                        summary.addRecord(record);
                    }
                } else {
                    summary.incrementMalformedLines();
                }
            }
        } catch (Exception e) {
            System.err.println(String.format(" ! [%s] I/O Error reading %s: %s", threadName, filePath, e.getMessage()));
            throw e;
        }

        long duration = System.currentTimeMillis() - startTime;
        summary.setExecutionTimeMs(duration);

        if (config.isVerboseLogging()) {
            System.out.println(String.format(" <- [%s] Finished %s in %d ms | Valid: %d | Errors: %d | Revenue: $%,.2f",
                    threadName, filePath, duration, summary.getValidRecords().size(), summary.getMalformedLines(), summary.getTotalNetRevenue()));
        }

        return summary;
    }

    /**
     * Parses a CSV row with format:
     * transactionId,date,region,customerType,category,product,unitsSold,unitPrice,discountRate,paymentMethod
     * Example: TX-8001,2026-01-15,North America,Retail,Electronics,UltraWide Monitor,3,380.00,0.05,Credit Card
     */
    private SalesTransaction parseTransactionLine(String line) {
        try {
            String[] parts = line.split(String.valueOf(config.getDelimiter()), -1);
            if (parts.length < 10) {
                // If legacy 4-column format is encountered, support backward compatibility
                if (parts.length == 4) {
                    String product = parts[0].trim();
                    String category = parts[1].trim();
                    int qty = Integer.parseInt(parts[2].trim());
                    double price = Double.parseDouble(parts[3].trim());
                    return new SalesTransaction("TX-LEGACY", LocalDate.now(), "General", "Retail",
                            category, product, qty, price, 0.0, "Cash");
                }
                return null;
            }

            String txId = parts[0].trim();
            LocalDate date = LocalDate.parse(parts[1].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            String region = parts[2].trim();
            String customerType = parts[3].trim();
            String category = parts[4].trim();
            String product = parts[5].trim();
            int unitsSold = Integer.parseInt(parts[6].trim());
            double unitPrice = Double.parseDouble(parts[7].trim());
            double discountRate = Double.parseDouble(parts[8].trim());
            String paymentMethod = parts[9].trim();

            if (unitsSold <= 0 || unitPrice < 0) {
                return null;
            }

            return new SalesTransaction(txId, date, region, customerType, category, product,
                    unitsSold, unitPrice, discountRate, paymentMethod);

        } catch (Exception e) {
            return null;
        }
    }
}
