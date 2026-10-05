package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result encapsulated by each parallel file processing worker task.
 */
public class ProcessingSummary {
    private final String filePath;
    private final String threadName;
    private final List<SalesTransaction> validRecords = new ArrayList<>();
    private int linesRead = 0;
    private int malformedLines = 0;
    private long executionTimeMs = 0;

    public ProcessingSummary(String filePath, String threadName) {
        this.filePath = filePath;
        this.threadName = threadName;
    }

    public void addRecord(SalesTransaction record) {
        validRecords.add(record);
    }

    public void incrementLinesRead() {
        this.linesRead++;
    }

    public void incrementMalformedLines() {
        this.malformedLines++;
    }

    public void setExecutionTimeMs(long ms) {
        this.executionTimeMs = ms;
    }

    public String getFilePath() { return filePath; }
    public String getThreadName() { return threadName; }
    public List<SalesTransaction> getValidRecords() { return validRecords; }
    public int getLinesRead() { return linesRead; }
    public int getMalformedLines() { return malformedLines; }
    public long getExecutionTimeMs() { return executionTimeMs; }

    public double getTotalNetRevenue() {
        return validRecords.stream().mapToDouble(SalesTransaction::getNetAmount).sum();
    }
}
