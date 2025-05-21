package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.util.Amount;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes the total revenue to a file.
 */
public class TotalRevenueFileOutput implements SaleObserver, AutoCloseable {
    private static final String REVENUE_LOG_FILE = "revenue-log.txt";
    private Amount totalRevenue = new Amount();
    private PrintWriter logFile;

    /**
     * Creates a new instance and prepares the log file.
     */
    public TotalRevenueFileOutput() {
        totalRevenue = new Amount();
        try {
            logFile = new PrintWriter(new FileWriter(REVENUE_LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Could not create revenue log file.");
            e.printStackTrace();
        }
    }

    /**
     * Updates the total revenue and writes it to a file when a new sale is made.
     *
     * @param totalPaid The amount paid for the sale that was just completed
     */
    @Override
    public void newSale(Amount totalPaid) {
        totalRevenue = totalRevenue.add(totalPaid);
        logCurrentRevenue(totalPaid);
    }

    /**
     * Closes the log file and releases resources.
     */
    @Override
    public void close() {
        if (logFile != null) {
            logFile.flush();
            logFile.close();
            System.out.println("Revenue file output closed.");
        }
    }

    private void logCurrentRevenue(Amount saleAmount) {
        logFile.println(createTimeStamp() +
                        ": New sale completed with amount " + saleAmount +
                        ". Total revenue is now: " + totalRevenue);
        logFile.flush();
    }

    private String createTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
