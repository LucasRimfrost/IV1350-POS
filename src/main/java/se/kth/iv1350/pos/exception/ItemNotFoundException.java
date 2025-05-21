package se.kth.iv1350.pos.exception;

/**
 * Thrown when trying to find an item that does not exist in the inventory.
 */
public class ItemNotFoundException extends RuntimeException {
    private final String itemID;

    /**
     * Creates a new instance with a message describing the error and
     * specifying which item could not be found.
     *
     * @param itemID The identifier of the item that could not be found.
     */
    public ItemNotFoundException(String itemID) {
        super("Could not find item with ID: " + itemID);
        this.itemID = itemID;
    }

    /**
     * Creates a new instance with a message and a cause.
     *
     * @param itemID The identifier of the item that could not be found.
     * @param cause The exception that caused this exception.
     */
    public ItemNotFoundException(String itemID, Throwable cause) {
        super("Could not find item with ID: " + itemID, cause);
        this.itemID = itemID;
    }

    /**
     * Gets the identifier of the item that could not be found.
     *
     * @return The item identifier.
     */
    public String getItemID() {
        return itemID;
    }
}
