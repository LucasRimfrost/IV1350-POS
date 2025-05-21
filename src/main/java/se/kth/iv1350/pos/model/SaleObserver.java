package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.util.Amount;

/**
 * A listener interface for receiving notifications about
 * completed sales. Classes that are interested in completed sales
 * implement this interface.
 */
public interface SaleObserver {
    /**
     * Called when a sale has been completed and paid.
     *
     * @param totalPaid The total amount paid for the sale
     */
    void newSale(Amount totalPaid);
}
