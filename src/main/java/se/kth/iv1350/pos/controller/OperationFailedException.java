package se.kth.iv1350.pos.controller;

/**
 * Thrown when an operation in the controller fails.
 * This is a runtime exception that wraps checked exceptions from lower layers,
 * allowing the view to handle them without having to declare throws clauses.
 */
public class OperationFailedException extends RuntimeException {

    /**
     * Creates a new instance with a message and cause.
     *
     * @param message The error message.
     * @param cause The exception that caused this exception.
     */
    public OperationFailedException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Gets the cause of this exception.
     * This is a convenience method to get the cause with the correct type.
     *
     * @return The exception that caused this exception.
     */
    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
