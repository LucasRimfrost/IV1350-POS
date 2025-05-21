package se.kth.iv1350.pos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents a receipt, which proves that a payment has been made.
 * Stores sale data rather than maintaining a reference to Sale.
 */
public class Receipt {
    private final List<SaleLineItem> items;
    private final Amount totalAmount;
    private final Amount totalVat;
    private final LocalDateTime saleTime;
    private final Amount paymentAmount;
    private final Amount changeAmount;

    /**
     * Creates a new instance with data extracted from sale.
     *
     * @param sale The sale this receipt is for
     * @param paymentAmount The amount paid by the customer
     * @param changeAmount The change given to the customer
     */
    public Receipt(Sale sale, Amount paymentAmount, Amount changeAmount) {
        // Extract data from sale through its public interface
        this.items = new ArrayList<>(sale.getItems());
        this.totalAmount = sale.calculateTotal();
        this.totalVat = sale.calculateTotalVat();
        this.saleTime = sale.getSaleTime();
        this.paymentAmount = paymentAmount;
        this.changeAmount = changeAmount;
    }

    /**
     * Gets the items on the receipt.
     *
     * @return An unmodifiable view of the items
     */
    public List<SaleLineItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the total amount on the receipt.
     *
     * @return The total amount
     */
    public Amount getTotalAmount() {
        return totalAmount;
    }

    /**
     * Gets the total VAT on the receipt.
     *
     * @return The total VAT
     */
    public Amount getTotalVat() {
        return totalVat;
    }

    /**
     * Gets the sale time on the receipt.
     *
     * @return The sale time
     */
    public LocalDateTime getSaleTime() {
        return saleTime;
    }

    /**
     * Gets the payment amount.
     *
     * @return The payment amount
     */
    public Amount getPaymentAmount() {
        return paymentAmount;
    }

    /**
     * Gets the change amount.
     *
     * @return The change amount
     */
    public Amount getChangeAmount() {
        return changeAmount;
    }
}
