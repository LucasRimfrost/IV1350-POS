package se.kth.iv1350.pos.integration;

import java.util.List;
import se.kth.iv1350.pos.model.SaleLineItem;

/**
 * Responsible for inventory management operations.
 * Separates inventory concerns from item information concerns.
 */
public class InventorySystem {
    private final ItemRegistry itemRegistry;

    /**
     * Creates a new instance with a reference to the item registry.
     *
     * @param itemRegistry The item registry to use
     */
    public InventorySystem(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /**
     * Updates inventory based on sold items.
     *
     * @param items The items sold
     * @return true if all inventory updates were successful
     */
    public boolean updateInventory(List<SaleLineItem> items) {
        boolean allSuccessful = true;

        System.out.println("Updating inventory for completed sale...");

        for (SaleLineItem item : items) {
            String itemID = item.getItem().itemID();
            int quantity = item.getQuantity();

            boolean success = decreaseInventoryQuantity(itemID, quantity);

            if (!success) {
                allSuccessful = false;
                System.out.println("Warning: Failed to update inventory for item: " + itemID);
            }
        }

        logUpdateResult(allSuccessful);

        return allSuccessful;
    }

    private boolean decreaseInventoryQuantity(String itemID, int quantity) {
        // In a real implementation, this would update a database
        // For this implementation, we delegate to the item registry
        boolean success = itemRegistry.decreaseInventoryQuantity(itemID, quantity);

        if (success) {
            System.out.println("Decreased inventory quantity of item " + itemID + " by " + quantity + " units.");
        }

        return success;
    }

    private void logUpdateResult(boolean allSuccessful) {
        if (allSuccessful) {
            System.out.println("Inventory successfully updated for all items");
        } else {
            System.out.println("Some inventory updates failed. Manual verification required.");
        }
    }
}
