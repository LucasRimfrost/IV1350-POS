package se.kth.iv1350.pos.model;

/**
 * Represents a command that can be executed as part of a sale operation.
 * This interface encapsulates a request as an object, allowing for parameterization
 * of clients with different requests, queuing of requests, and logging of operations.
 *
 * This follows the Command design pattern, enabling undo functionality,
 * macro recording, and decoupling of the object that invokes the operation
 * from the object that performs the operation.
 */
public interface SaleCommand {

    /**
     * Executes the command operation. This method performs the actual work
     * that the command encapsulates.
     *
     * @throws RuntimeException if the command execution fails for any reason.
     */
    void execute();

    /**
     * Provides a human-readable description of what this command does.
     * This description can be used for logging, debugging, or displaying
     * operation history to users.
     *
     * @return A descriptive string explaining the command's purpose and parameters.
     */
    String getDescription();
}
