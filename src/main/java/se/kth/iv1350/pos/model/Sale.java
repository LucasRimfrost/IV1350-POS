package se.kth.iv1350.pos.model;

import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents a single sale transaction.
 * Handles items, pricing, discounts, payments, and receipt generation.
 */
public class Sale {
    private final List<SaleLineItem> items;
    private Receipt receipt;

    /**
     * Creates a new instance, representing the beginning of a sale.
     */
    public Sale() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds an item to the sale or increases quantity if already present.
     *
     * @param itemDTO The item to add.
     * @param quantity The quantity of the specified item.
     */
    public void addItem(ItemDTO itemDTO, int quantity) {
        SaleLineItem existingItem = findExistingItem(itemDTO.getItemID());

        if (existingItem != null) {
            existingItem.incrementQuantity(quantity);
        } else {
            addNewItem(itemDTO, quantity);
        }
    }

    /**
     * Gets the list of items in the sale.
     *
     * @return A copy of the list of sale line items.
     */
    public List<SaleLineItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Calculates the total price of all items, excluding VAT.
     *
     * @return The total price, excluding VAT.
     */
    public Amount calculateTotal() {
        Amount total = new Amount();
        for (SaleLineItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    /**
     * Calculates the total VAT for all items.
     *
     * @return The total VAT amount.
     */
    public Amount calculateTotalVat() {
        Amount totalVat = new Amount();
        for (SaleLineItem item : items) {
            totalVat = totalVat.add(item.getVatAmount());
        }
        return totalVat;
    }

    /**
     * Gets the total price including VAT and applying any discounts.
     *
     * @return The total price including VAT, after discounts.
     */
    public Amount calculateTotalWithVat() {
        return calculateTotal().add(calculateTotalVat());
    }

    /**
     * Processes payment for this sale and generates a receipt.
     * Notifies observers about the completed sale.
     *
     * @param payment The payment used to pay for the sale.
     * @return The change amount to be given back to the customer.
     */
    public Amount processPayment(CashPayment payment) {
        Amount totalToPay = calculateTotalWithVat();
        Amount change = payment.getChange(totalToPay);

        this.receipt = new Receipt(this, payment.getAmount(), change);
        return change;
    }

    /**
     * Prints a receipt for the current sale on the specified printer.
     *
     * @param printer The printer where the receipt is printed.
     */
    public void printReceipt(Printer printer) {
        if (receipt != null) {
            printer.printReceipt(receipt);
        }
    }

    /**
     * Updates inventory with all items in this sale.
     *
     * @param itemRegistry The registry to update inventory in
     * @return true if all updates were successful, false if any failed
     */
    public boolean updateInventory(ItemRegistry itemRegistry) {
        return itemRegistry.updateInventoryForCompletedSale(items);
    }

    private SaleLineItem findExistingItem(String itemID) {
        for (SaleLineItem item : items) {
            if (item.getItem().getItemID().equals(itemID)) {
                return item;
            }
        }
        return null;
    }

    private void addNewItem(ItemDTO itemDTO, int quantity) {
        SaleLineItem newLineItem = new SaleLineItem(itemDTO, quantity);
        items.add(newLineItem);
    }
}
