package se.kth.iv1350.pos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents a single sale transaction.
 * Focuses on core sale data and calculations only.
 */
public class Sale {
    private final List<SaleLineItem> items;
    private final LocalDateTime saleTime;
    private Amount discountAmount;
    private String customerID;

    /**
     * Creates a new sale instance.
     */
    public Sale() {
        this.items = new ArrayList<>();
        this.saleTime = LocalDateTime.now();
        this.discountAmount = new Amount();
    }

    /**
     * Adds an item to the sale.
     *
     * @param itemDTO The item to add
     * @param quantity The quantity to add
     */
    public void addItem(ItemDTO itemDTO, int quantity) {
        SaleLineItem existingItem = findItem(itemDTO.itemID());

        if (existingItem != null) {
            existingItem.incrementQuantity(quantity);
        } else {
            items.add(new SaleLineItem(itemDTO, quantity));
        }
    }

    /**
     * Gets all sale line items (as an unmodifiable list for safety).
     *
     * @return An unmodifiable view of the items list
     */
    public List<SaleLineItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calculates the total price excluding VAT.
     *
     * @return The total price without VAT
     */
    public Amount calculateTotal() {
        Amount total = new Amount();
        for (SaleLineItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Calculates the total VAT amount.
     *
     * @return The total VAT
     */
    public Amount calculateTotalVat() {
        Amount totalVat = new Amount();
        for (SaleLineItem item : items) {
            totalVat = totalVat.add(item.getVatAmount());
        }
        return totalVat;
    }

    /**
     * Calculates the total price including VAT and applying discounts.
     *
     * @return The total price with VAT and after discounts
     */
    public Amount calculateTotalWithVat() {
        Amount total = calculateTotal().add(calculateTotalVat());
        return total.subtract(discountAmount);
    }

    /**
     * Applies a discount to this sale.
     *
     * @param customerID The customer ID receiving the discount
     * @param discountAmount The discount amount
     */
    public void applyDiscount(String customerID, Amount discountAmount) {
        this.customerID = customerID;
        this.discountAmount = discountAmount;
    }

    /**
     * Gets the discount amount.
     *
     * @return The discount amount
     */
    public Amount getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Gets the sale time.
     *
     * @return The time when this sale was created
     */
    public LocalDateTime getSaleTime() {
        return saleTime;
    }

    /**
     * Gets the customer ID if any.
     *
     * @return The customer ID or null if none
     */
    public String getCustomerID() {
        return customerID;
    }

    /**
     * Creates a receipt for this sale.
     *
     * @param paymentAmount The amount paid
     * @param changeAmount The change given
     * @return A receipt for this sale
     */
    public Receipt createReceipt(Amount paymentAmount, Amount changeAmount) {
        return new Receipt(this, paymentAmount, changeAmount);
    }

    private SaleLineItem findItem(String itemID) {
        for (SaleLineItem item : items) {
            if (item.getItem().itemID().equals(itemID)) {
                return item;
            }
        }
        return null;
    }
}
