package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.util.Amount;

/**
 * Interface for different types of payments.
 * This allows for different payment methods to be implemented.
 */
public interface Payment {
    /**
     * Gets the amount paid.
     *
     * @return The amount paid.
     */
    Amount getAmount();
}
