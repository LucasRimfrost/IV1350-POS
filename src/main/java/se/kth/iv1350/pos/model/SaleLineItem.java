package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents a line item in a sale.
 */
public class SaleLineItem {
    private final ItemDTO item;
    private int quantity;

    /**
     * Creates a new line item.
     *
     * @param item The item this line represents
     * @param quantity The quantity of the item
     */
    public SaleLineItem(ItemDTO item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * Increases the quantity of this line item.
     *
     * @param quantityToAdd The quantity to add
     */
    public void incrementQuantity(int quantityToAdd) {
        this.quantity += quantityToAdd;
    }

    /**
     * Gets the subtotal price excluding VAT.
     *
     * @return The subtotal excluding VAT
     */
    public Amount getSubtotal() {
        return item.price().multiply(quantity);
    }

    /**
     * Gets the VAT amount for this line item.
     *
     * @return The VAT amount
     */
    public Amount getVatAmount() {
        return calculateVatAmount().multiply(quantity);
    }

    /**
     * Gets the total price including VAT.
     *
     * @return The total with VAT
     */
    public Amount getTotalWithVat() {
        return getPriceWithVat().multiply(quantity);
    }

    /**
     * Gets the item.
     *
     * @return The item
     */
    public ItemDTO getItem() {
        return item;
    }

    /**
     * Gets the quantity.
     *
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }

    private Amount calculateVatAmount() {
        return item.price().multiply(item.vatRate());
    }

    private Amount getPriceWithVat() {
        return item.price().add(calculateVatAmount());
    }
}
