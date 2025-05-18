package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.util.Amount;

/**
 * An observer interface for receiving notifications about completed sales.
 * Classes that implement this interface can be notified when a sale is completed.
 */
public interface SaleObserver {
    /**
     * Called when a sale has been paid for.
     *
     * @param totalAmount The total amount paid for the sale.
     */
    void newSaleCompleted(Amount totalAmount);
}
