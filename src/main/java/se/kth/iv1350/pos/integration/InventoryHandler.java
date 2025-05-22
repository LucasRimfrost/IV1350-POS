package se.kth.iv1350.pos.integration;

import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import se.kth.iv1350.pos.model.SaleLineItem;

/**
 * Concrete handler for processing completed sales with the inventory system.
 * This class implements the Handler pattern by providing specific handling
 * logic for inventory system integration.
 *
 * Part of the GoF Handler pattern implementation.
 */
public class InventoryHandler extends SaleCompletionHandler {
    private final InventorySystem inventorySystem;

    /**
     * Creates a new inventory handler with the specified inventory system.
     *
     * @param inventorySystem The inventory system to update with sold items. Must not be null.
     */
    public InventoryHandler(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    /**
     * Handles the completed sale by updating inventory quantities for all sold items.
     * This implements the specific handling behavior for inventory integration.
     *
     * @param sale The completed sale data containing items and quantities sold.
     */
    @Override
    public void handle(SaleDTO sale) {
        List<SaleLineItem> items = convertToSaleLineItems(sale.items());
        inventorySystem.updateInventory(items);
    }

    /**
     * Returns the name identifier for this inventory handler.
     *
     * @return The string "Inventory Handler".
     */
    @Override
    public String getHandlerName() {
        return "Inventory Handler";
    }

    /**
     * Converts SaleItemDTO objects to SaleLineItem objects required by inventory system.
     */
    private List<SaleLineItem> convertToSaleLineItems(List<SaleItemDTO> saleItems) {
        List<SaleLineItem> lineItems = new ArrayList<>();
        for (SaleItemDTO item : saleItems) {
            lineItems.add(new SaleLineItem(item.item(), item.quantity()));
        }
        return lineItems;
    }
}
