package se.kth.iv1350.pos.integration;

import java.util.HashMap;
import java.util.Map;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external inventory system.
 * This class is responsible for retrieving item information.
 */
public class ItemRegistry {
    private Map<String, ItemDTO> items = new HashMap<>();

    /**
     * Creates a new instance and initializes with some default test items.
     */
    public ItemRegistry() {
        addTestItems();
    }

    /**
     * Searches for an item with the specified identifier.
     *
     * @param itemID The item identifier.
     * @return The DTO containing item information, or null if no matching item was found.
     */
    public ItemDTO findItem(String itemID) {
        return items.get(itemID);
    }

    /**
     * Updates the inventory with the specified item quantity.
     * This method would connect to the external inventory system in a production environment.
     *
     * @param itemID The item identifier.
     * @param quantity The quantity to decrease from inventory.
     */
    public void updateInventory(String itemID, int quantity) {
        // This would call the external inventory system in a real implementation
        System.out.println("Inventory updated for item: " + itemID +
                         ", quantity: -" + quantity);
    }

    private void addTestItems() {
        items.put("1", new ItemDTO("1", "Apple", new Amount(10.0), 0.12));
        items.put("2", new ItemDTO("2", "Orange", new Amount(15.0), 0.12));
        items.put("3", new ItemDTO("3", "Milk", new Amount(22.0), 0.12));
        items.put("4", new ItemDTO("4", "Bread", new Amount(30.0), 0.25));
        items.put("5", new ItemDTO("5", "Cheese", new Amount(75.0), 0.25));
    }
}
