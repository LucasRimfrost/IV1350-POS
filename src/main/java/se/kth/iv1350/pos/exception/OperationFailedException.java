package se.kth.iv1350.pos.exception;

/**
 * Thrown when an operation in the controller layer fails for any reason.
 * This exception wraps exceptions from lower layers and provides a
 * higher level of abstraction for the view layer.
 */
public class OperationFailedException extends Exception {

    /**
     * Creates a new instance with a message describing the error.
     *
     * @param message A description of the error.
     */
    public OperationFailedException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with a message and a cause.
     *
     * @param message A description of the error.
     * @param cause The exception that caused this exception.
     */
    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
