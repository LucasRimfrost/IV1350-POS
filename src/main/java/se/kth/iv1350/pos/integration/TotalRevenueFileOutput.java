package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.util.Amount;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes the total revenue to a file.
 * This class is an observer that gets notified when sales are completed.
 */
public class TotalRevenueFileOutput implements SaleObserver {
    private static final String REVENUE_LOG_FILE = "revenue-log.txt";
    private Amount totalRevenue;
    private PrintWriter logWriter;

    /**
     * Creates a new instance with zero initial revenue.
     * Opens the file where revenue information will be stored.
     */
    public TotalRevenueFileOutput() {
        this.totalRevenue = new Amount();
        try {
            logWriter = new PrintWriter(new FileWriter(REVENUE_LOG_FILE, true), true);
        } catch (IOException ex) {
            System.err.println("Could not create revenue logger: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Updates the total revenue when a new sale is completed.
     * Writes the updated revenue to the log file.
     *
     * @param saleAmount The amount of the completed sale.
     */
    @Override
    public void newSaleCompleted(Amount saleAmount) {
        totalRevenue = totalRevenue.add(saleAmount);
        logCurrentRevenue();
    }

    /**
     * Logs the current total revenue to the file.
     */
    private void logCurrentRevenue() {
        if (logWriter != null) {
            logWriter.println(getCurrentTime() + ": Total Revenue: " + totalRevenue);
        }
    }

    /**
     * Gets the current time formatted for log entries.
     *
     * @return The formatted current time string.
     */
    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Closes the revenue log file.
     * This method is automatically called when the logger is used with try-with-resources.
     */
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}
