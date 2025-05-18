package se.kth.iv1350.pos.exception;

/**
 * Thrown when the database server cannot be called.
 * This is a checked exception that indicates that the external inventory
 * database or catalog system is unavailable.
 */
public class DatabaseConnectionException extends Exception {

    /**
     * Creates a new instance with a message describing the error.
     *
     * @param message The error message.
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with a message and cause.
     *
     * @param message The error message.
     * @param cause The exception that caused this exception.
     */
    public DatabaseConnectionException(String message, Exception cause) {
        super(message, cause);
    }
}
