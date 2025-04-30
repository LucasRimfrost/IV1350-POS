package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * This is a placeholder for the real view. It contains hardcoded execution with calls
 * to the controller, simulating user interactions that would be triggered by GUI events.
 */
public class View {
    private final Controller controller;

    /**
     * Creates a new instance.
     *
     * @param controller The controller that is used for all operations.
     */
    public View(Controller controller) {
        this.controller = controller;
    }

    /**
     * Simulates a user interaction with the system.
     */
    public void runFakeExecution() {
        // Start new sale
        System.out.println("Starting new sale...");
        controller.startNewSale();

        // Scan items
        System.out.println("Scanning items...");
        scanItem("1", 2);   // 2 Apples
        scanItem("3", 1);   // 1 Milk
        scanItem("2", 3);   // 3 Oranges
        scanItem("999", 1); // Invalid item ID
        scanItem("3", 2);   // 2 more Milk (should add to existing)

        // End sale
        System.out.println("\nEnding sale...");
        Amount total = controller.endSale();
        System.out.println("Total with VAT: " + total);

        // Request discount
        System.out.println("\nCustomer requests discount with ID 1001...");
        Amount discountedTotal = controller.requestDiscount("1001");
        System.out.println("Total after discount: " + discountedTotal);

        // Pay
        System.out.println("\nCustomer pays 200 SEK...");
        controller.pay(new Amount(200));

        System.out.println("\nSale completed.");
    }

    private void scanItem(String itemID, int quantity) {
        System.out.println("Scanning item ID: " + itemID + ", quantity: " + quantity);
        ItemDTO item = controller.enterItem(itemID, quantity);
        if (item != null) {
            System.out.println("Added: " + item.getDescription() +
                             ", price: " + item.getPrice() +
                             ", VAT rate: " + (item.getVatRate() * 100) + "%");
        } else {
            System.out.println("Item not found!");
        }
    }
}
