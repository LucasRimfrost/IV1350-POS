package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.dto.SaleDTO;

/**
 * Concrete handler for processing completed sales with the accounting system.
 * This class implements the Handler pattern by providing specific handling
 * logic for accounting system integration.
 *
 * Part of the GoF Handler pattern implementation.
 */
public class AccountingHandler extends SaleCompletionHandler {
    private final AccountingSystem accountingSystem;

    /**
     * Creates a new accounting handler with the specified accounting system.
     *
     * @param accountingSystem The accounting system to send sale data to. Must not be null.
     */
    public AccountingHandler(AccountingSystem accountingSystem) {
        this.accountingSystem = accountingSystem;
    }

    /**
     * Handles the completed sale by sending sale information to the accounting system.
     * This implements the specific handling behavior for accounting integration.
     *
     * @param sale The completed sale data to be recorded.
     */
    @Override
    public void handle(SaleDTO sale) {
        accountingSystem.recordSale(sale);
    }

    /**
     * Returns the name identifier for this accounting handler.
     *
     * @return The string "Accounting Handler".
     */
    @Override
    public String getHandlerName() {
        return "Accounting Handler";
    }
}
