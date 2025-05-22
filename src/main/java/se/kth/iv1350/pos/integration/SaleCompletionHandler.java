package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.dto.SaleDTO;

/**
 * Abstract base class implementing the Handler part of the Handler design pattern.
 * This pattern allows different concrete handlers to process completed sales
 * in their own specific way, promoting loose coupling and easy extensibility.
 *
 * Each concrete handler is responsible for integrating with one specific
 * external system (accounting, inventory, etc.) when a sale is completed.
 *
 * This is part of the GoF Handler pattern implementation.
 */
public abstract class SaleCompletionHandler {

    /**
     * Handles the processing of a completed sale for a specific external system.
     * This method implements the core Handler pattern behavior where each
     * concrete handler processes the sale in its own specific way.
     *
     * @param sale The completed sale data to be processed. Must not be null.
     */
    public abstract void handle(SaleDTO sale);

    /**
     * Returns the name of this handler for identification and logging purposes.
     *
     * @return A descriptive name for this handler, never null.
     */
    public abstract String getHandlerName();
}
