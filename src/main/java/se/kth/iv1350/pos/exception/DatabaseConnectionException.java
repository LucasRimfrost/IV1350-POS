package se.kth.iv1350.pos.exception;

/**
 * Thrown when the application is unable to connect to the database.
 */
public class DatabaseConnectionException extends Exception {

    /**
     * Creates a new instance with a message describing the error.
     *
     * @param message A description of the error.
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with a message and a cause.
     *
     * @param message A description of the error.
     * @param cause The exception that caused this exception.
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
