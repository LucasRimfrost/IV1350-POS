package se.kth.iv1350.pos.integration;

import java.util.HashMap;
import java.util.Map;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains operations related to item information.
 * Focused on finding items and their properties, not inventory management.
 */
public class ItemRegistry {
    private static final String DATABASE_ERROR_TRIGGER_ITEM_ID = "9999";

    private final Map<String, ItemDTO> items = new HashMap<>();
    private final Map<String, Integer> inventory = new HashMap<>();

    private static final ItemRegistry instance = new ItemRegistry();

    /**
     * Gets the singleton instance of the ItemRegistry.
     *
     * @return The singleton instance.
     */
    public static ItemRegistry getInstance() {
        return instance;
    }


    /**
     * Searches for an item with the specified identifier.
     *
     * @param itemID The item identifier
     * @return The item DTO
     * @throws ItemNotFoundException if the item does not exist in inventory
     * @throws DatabaseConnectionException if a database connection error occurs
     */
    public ItemDTO findItem(String itemID) throws ItemNotFoundException, DatabaseConnectionException {
        if (DATABASE_ERROR_TRIGGER_ITEM_ID.equals(itemID)) {
            throw new DatabaseConnectionException("Could not connect to inventory database");
        }

        ItemDTO item = items.get(itemID);

        if (item == null) {
            throw new ItemNotFoundException(itemID);
        }

        return item;
    }

    /**
     * Checks if sufficient quantity of an item is available.
     *
     * @param itemID The item identifier
     * @param quantity The quantity to check
     * @return true if sufficient quantity is available
     */
    public boolean isItemAvailable(String itemID, int quantity) {
        Integer available = inventory.get(itemID);
        return available != null && available >= quantity;
    }

    /**
     * Decreases the inventory quantity for an item.
     * This is package-private as it should only be called by InventorySystem.
     *
     * @param itemID The item identifier
     * @param quantity The quantity to decrease
     * @return true if successful, false otherwise
     */
    boolean decreaseInventoryQuantity(String itemID, int quantity) {
        Integer currentQuantity = inventory.get(itemID);

        if (currentQuantity == null || currentQuantity < quantity) {
            return false;
        }

        inventory.put(itemID, currentQuantity - quantity);
        return true;
    }

    private void loadTestItemCatalog() {
        items.put("1", new ItemDTO("1",
                "Kellogg's Cornflakes",
                "500g, whole grain, fortified with vitamins",
                new Amount(10.0), 0.12));

        items.put("2", new ItemDTO("2",
                "Barilla Pasta",
                "500g, spaghetti, bronze cut",
                new Amount(15.0), 0.12));

        items.put("3", new ItemDTO("3",
                "Arla Milk",
                "1L, organic whole milk, pasteurized",
                new Amount(22.0), 0.12));

        items.put("4", new ItemDTO("4",
                "Wasa Crispbread",
                "275g, whole grain, low sugar",
                new Amount(30.0), 0.25));

        items.put("5", new ItemDTO("5",
                "Fazer Chocolate",
                "200g, milk chocolate, Finnish quality",
                new Amount(75.0), 0.25));
    }

    private void initializeTestInventory() {
        for (String itemID : items.keySet()) {
            inventory.put(itemID, 50);
        }
    }

    private ItemRegistry() {
        loadTestItemCatalog();
        initializeTestInventory();
    }
}
