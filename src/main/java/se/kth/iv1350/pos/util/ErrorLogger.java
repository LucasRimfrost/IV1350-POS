package se.kth.iv1350.pos.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is responsible for logging error messages to a file.
 */
public class ErrorLogger {
    private static final String LOG_FILE = "pos-error-log.txt";
    private PrintWriter logWriter;

    /**
     * Creates a new instance and opens the log file.
     * If the file cannot be opened, an error message is printed to the standard error.
     */
    public ErrorLogger() {
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException ex) {
            System.err.println("Could not create logger: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Logs an exception to the log file with a timestamp, exception class, and message.
     * The stack trace is also included in the log.
     *
     * @param exception The exception to log.
     */
    public void logException(Exception exception) {
        if (logWriter != null) {
            StringBuilder logMsg = new StringBuilder();
            logMsg.append(getCurrentTime()).append(": ");
            logMsg.append(exception.getClass().getName()).append(": ");
            logMsg.append(exception.getMessage());
            logWriter.println(logMsg);
            exception.printStackTrace(logWriter);
            logWriter.println("-------------------");
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
     * Closes the log file.
     * This method is automatically called when the logger is used with try-with-resources.
     */
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}
