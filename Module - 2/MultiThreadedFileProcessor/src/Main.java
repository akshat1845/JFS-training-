import config.ProcessorConfig;
import model.ProcessingSummary;
import processor.CsvFileProcessor;
import processor.SampleDataGenerator;
import report.AnalyticsReportAggregator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * ==============================================================================
 *  Module 2: High-Performance Multi-Threaded Sales Data & Revenue Processor
 * ==============================================================================
 *
 * Architecture & Concurrency Strategy:
 *  1. Configuration Initialization via Builder Pattern (ProcessorConfig.Builder)
 *  2. Input Partition Scan: Dynamically queries the 'data/' directory for CSV sets
 *  3. Sample Data Self-Bootstrapping: Populates multi-regional test data if absent
 *  4. Parallel Thread Pool (ExecutorService): Fixed/Dynamic thread pooling
 *  5. Concurrent Task Submission: Each file processed asynchronously as a Callable<ProcessingSummary>
 *  6. Futures Collection & Barrier Await: Non-blocking future aggregation with error containment
 *  7. Graceful Thread Pool Termination
 *  8. Multi-Dimensional Analytics Generation: Formatted text report + CSV export
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println("   ENTERPRISE MULTI-THREADED SALES & FINANCIAL DATA PROCESSOR (MODULE 2)");
        System.out.println("==============================================================================\n");

        long programStartTime = System.currentTimeMillis();

        // ----------------------------------------------------------------------
        // STEP 1: Configure processing engine via Fluent Builder Pattern
        // ----------------------------------------------------------------------
        ProcessorConfig config = new ProcessorConfig.Builder()
                .threadCount(4)                          // 4 concurrent worker threads
                .inputFolder("data/")                    // Input folder
                .reportFile("analytics_report.txt")      // Text analytical summary report
                .categoryCsvExport("category_summary.csv")// Exportable CSV summary
                .skipHeader(true)                        // Skip CSV header line
                .delimiter(',')                          // Comma-delimited
                .minTransactionFilter(0.0)               // Minimum net amount threshold
                .verboseLogging(true)                    // Enable thread lifecycle logging
                .build();

        System.out.println("[CONFIG] Engine Configuration: " + config + "\n");

        // ----------------------------------------------------------------------
        // STEP 2: Ensure Sample Data Exists & Scan Directory
        // ----------------------------------------------------------------------
        SampleDataGenerator.generateSampleDatasetsIfEmpty(config.getInputFolder());

        File folder = new File(config.getInputFolder());
        File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            System.err.println("[FATAL] No CSV files found in directory: " + config.getInputFolder());
            return;
        }

        System.out.println(String.format("[DISCOVERY] Found %d CSV dataset partition(s) for parallel processing:", csvFiles.length));
        for (File f : csvFiles) {
            System.out.println("  • " + f.getName() + " (" + f.length() + " bytes)");
        }
        System.out.println();

        // ----------------------------------------------------------------------
        // STEP 3: Initialize ExecutorService Thread Pool
        // ----------------------------------------------------------------------
        int poolSize = Math.min(config.getThreadCount(), csvFiles.length);
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WorkerThread-" + counter++);
            }
        });

        System.out.println(String.format("[THREAD-POOL] Initialized pool with %d worker thread(s).\n", poolSize));

        // ----------------------------------------------------------------------
        // STEP 4: Submit Callable Tasks to Worker Threads
        // ----------------------------------------------------------------------
        List<Future<ProcessingSummary>> futures = new ArrayList<>();
        for (File file : csvFiles) {
            CsvFileProcessor task = new CsvFileProcessor(file.getPath(), config);
            Future<ProcessingSummary> future = executorService.submit(task);
            futures.add(future);
        }

        // ----------------------------------------------------------------------
        // STEP 5: Collect Future Results & Aggregate Metrics
        // ----------------------------------------------------------------------
        AnalyticsReportAggregator aggregator = new AnalyticsReportAggregator();

        for (Future<ProcessingSummary> future : futures) {
            try {
                ProcessingSummary summary = future.get(); // Awaits completion
                aggregator.addSummary(summary);
            } catch (InterruptedException e) {
                System.err.println("[INTERRUPTED] Processing interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                System.err.println("[EXECUTION ERROR] Exception in worker: " + e.getCause().getMessage());
            }
        }

        // ----------------------------------------------------------------------
        // STEP 6: Graceful Shutdown of Thread Pool
        // ----------------------------------------------------------------------
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        long totalExecutionTime = System.currentTimeMillis() - programStartTime;
        System.out.println("\n[SYSTEM] All parallel worker tasks completed successfully.");

        // ----------------------------------------------------------------------
        // STEP 7: Generate Multi-Faceted Reports & CSV Exports
        // ----------------------------------------------------------------------
        aggregator.generateReports(config, totalExecutionTime);

        System.out.println("\n[COMPLETE] Multi-threaded processing workflow finished successfully in " + totalExecutionTime + " ms.");
    }
}
