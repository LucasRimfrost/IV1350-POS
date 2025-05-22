package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.dto.ItemDTO;

/**
 * A command that adds a specified quantity of an item to a sale.
 * This command encapsulates the operation of adding items to a sale,
 * allowing for consistent handling of item addition throughout the system.
 *
 * If an item with the same identifier already exists in the sale,
 * the quantity will be incremented rather than creating a duplicate entry.
 * This behavior is handled by the underlying Sale object.
 */
public class AddItemCommand implements SaleCommand {
    private final Sale sale;
    private final ItemDTO item;
    private final int quantity;

    /**
     * Creates a new command to add an item to a sale.
     *
     * @param sale The sale to which the item should be added. Must not be null.
     * @param item The item to add to the sale. Must not be null.
     * @param quantity The quantity of the item to add. Must be positive.
     *
     * @throws IllegalArgumentException if any parameter is null or if quantity is not positive.
     */
    public AddItemCommand(Sale sale, ItemDTO item, int quantity) {
        this.sale = sale;
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * Executes the add item operation by calling the sale's addItem method.
     * This will either add a new line item to the sale or increment the quantity
     * of an existing line item if the same item is already present.
     */
    @Override
    public void execute() {
        sale.addItem(item, quantity);
    }

    /**
     * Returns a description of this add item command, including the quantity
     * and item name for easy identification in logs or command history.
     *
     * @return A formatted string describing the item addition operation.
     */
    @Override
    public String getDescription() {
        return "Add " + quantity + "x " + item.name() + " to sale";
    }
}
