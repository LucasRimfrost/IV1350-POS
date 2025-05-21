package se.kth.iv1350.pos.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is responsible for logging exceptions and other information
 * that might be useful for troubleshooting.
 */
public class ErrorLogger implements AutoCloseable {
    private static final String LOG_FILE_NAME = "pos-error-log.txt";
    private PrintWriter logFile;

    /**
     * Creates a new instance and opens the log file.
     */
    public ErrorLogger() {
        try {
            logFile = new PrintWriter(new FileWriter(LOG_FILE_NAME, true), true);
        } catch (IOException e) {
            System.err.println("Could not create log file.");
            e.printStackTrace();
        }
    }

    /**
     * Logs the specified exception.
     *
     * @param exception The exception to log.
     */
    public void logException(Exception exception) {
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append(createTime());
        logMsgBuilder.append(", Exception was thrown: ");
        logMsgBuilder.append(exception.getMessage());
        logMsgBuilder.append("\n");
        logMsgBuilder.append("Stack Trace:\n");

        logFile.println(logMsgBuilder.toString());
        exception.printStackTrace(logFile);
        logFile.println("\n");
    }

    /**
     * Logs the specified message.
     *
     * @param message The message to log.
     */
    public void logMessage(String message) {
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append(createTime());
        logMsgBuilder.append(", ");
        logMsgBuilder.append(message);

        logFile.println(logMsgBuilder.toString());
    }

    /**
     * Closes the log file.
     */
    @Override
    public void close() {
        if (logFile != null) {
            logFile.flush();
            logFile.close();
            System.out.println("Error logger closed.");
        }
    }

    private String createTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}
