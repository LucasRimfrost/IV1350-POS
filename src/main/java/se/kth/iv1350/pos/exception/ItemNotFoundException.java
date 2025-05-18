package se.kth.iv1350.pos.exception;

/**
 * Thrown when an item identifier does not exist in the inventory.
 * This is a checked exception that indicates a search for an
 * invalid item identifier that was not found in the inventory catalog.
 */
public class ItemNotFoundException extends Exception {
    private final String itemID;

    /**
     * Creates a new instance with a message describing the error and
     * the specified invalid item identifier.
     *
     * @param itemID The invalid item identifier.
     */
    public ItemNotFoundException(String itemID) {
        super("Item with ID " + itemID + " was not found in inventory.");
        this.itemID = itemID;
    }

    /**
     * Gets the identifier that could not be found.
     *
     * @return The invalid item identifier.
     */
    public String getItemID() {
        return itemID;
    }
}
