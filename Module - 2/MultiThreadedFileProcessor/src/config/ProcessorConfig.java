package config;

/**
 * Enhanced configuration holder with Fluent Builder Pattern.
 */
public class ProcessorConfig {
    private final int threadCount;
    private final String inputFolder;
    private final String reportFile;
    private final String categoryCsvExport;
    private final boolean skipHeader;
    private final char delimiter;
    private final double minTransactionFilter;
    private final boolean verboseLogging;

    private ProcessorConfig(Builder builder) {
        this.threadCount = builder.threadCount;
        this.inputFolder = builder.inputFolder;
        this.reportFile = builder.reportFile;
        this.categoryCsvExport = builder.categoryCsvExport;
        this.skipHeader = builder.skipHeader;
        this.delimiter = builder.delimiter;
        this.minTransactionFilter = builder.minTransactionFilter;
        this.verboseLogging = builder.verboseLogging;
    }

    public int getThreadCount() { return threadCount; }
    public String getInputFolder() { return inputFolder; }
    public String getReportFile() { return reportFile; }
    public String getCategoryCsvExport() { return categoryCsvExport; }
    public boolean isSkipHeader() { return skipHeader; }
    public char getDelimiter() { return delimiter; }
    public double getMinTransactionFilter() { return minTransactionFilter; }
    public boolean isVerboseLogging() { return verboseLogging; }

    @Override
    public String toString() {
        return String.format(
            "ProcessorConfig{threads=%d, inputDir='%s', reportFile='%s', exportCsv='%s', delimiter='%c', minAmount=%.2f, verbose=%b}",
            threadCount, inputFolder, reportFile, categoryCsvExport, delimiter, minTransactionFilter, verboseLogging
        );
    }

    public static class Builder {
        private int threadCount = Runtime.getRuntime().availableProcessors();
        private String inputFolder = "data/";
        private String reportFile = "analytics_report.txt";
        private String categoryCsvExport = "category_summary.csv";
        private boolean skipHeader = true;
        private char delimiter = ',';
        private double minTransactionFilter = 0.0;
        private boolean verboseLogging = true;

        public Builder threadCount(int threads) {
            if (threads < 1) throw new IllegalArgumentException("Thread count must be at least 1");
            this.threadCount = threads;
            return this;
        }

        public Builder inputFolder(String path) {
            this.inputFolder = path;
            return this;
        }

        public Builder reportFile(String reportFile) {
            this.reportFile = reportFile;
            return this;
        }

        public Builder categoryCsvExport(String exportPath) {
            this.categoryCsvExport = exportPath;
            return this;
        }

        public Builder skipHeader(boolean skip) {
            this.skipHeader = skip;
            return this;
        }

        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder minTransactionFilter(double minAmount) {
            this.minTransactionFilter = minAmount;
            return this;
        }

        public Builder verboseLogging(boolean verbose) {
            this.verboseLogging = verbose;
            return this;
        }

        public ProcessorConfig build() {
            return new ProcessorConfig(this);
        }
    }
}
