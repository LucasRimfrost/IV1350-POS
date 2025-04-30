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
    private Amount runningTotal = new Amount();

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
     * This method performs a complete sale following the basic flow from requirements.
     */
    public void runFakeExecution() {
        // Start new sale
        System.out.println("Starting new sale...");
        controller.startNewSale();
        runningTotal = new Amount();

        // Add 1 item with item id abc123
        System.out.println("Add 1 item with item id 1:");
        scanItem("1", 1);   // Using ID 1 from our database (Apple)

        // Add same item again
        System.out.println("Add 1 item with item id 1:");
        scanItem("1", 1);   // Same item, should be treated as duplicate

        // Add a different item
        System.out.println("Add 1 item with item id 3:");
        scanItem("3", 1);   // Using ID 3 from our database (Milk)

        System.out.println("Add 1 item with item id 2:");
        scanItem("2", 1);

        // End sale
        System.out.println("End sale :");
        Amount total = controller.endSale();
        System.out.println("Total cost (incl VAT): " + total);

        // Payment
        Amount paymentAmount = new Amount(100);
        System.out.println("Customer pays " + paymentAmount + ":");
        Amount change = controller.pay(paymentAmount);

        // Final message about change
        System.out.println("\nChange to give the customer: " + change);
    }

    private void scanItem(String itemID, int quantity) {
        Controller.ItemWithRunningTotal result = controller.enterItem(itemID, quantity);

        if (result != null) {
            ItemDTO item = result.getItem();
            runningTotal = result.getRunningTotal();
            boolean isDuplicate = result.isDuplicate();

            // Display item information in the required format
            System.out.println("Item ID : " + item.getItemID());
            System.out.println("Item name : " + item.getName());
            System.out.println("Item cost : " + formatAmount(item.getPrice()) + " SEK");
            System.out.println("VAT : " + (int)(item.getVatRate() * 100) + "%");
            System.out.println("Item description : " + item.getDescription());
            System.out.println();

            if (isDuplicate) {
                System.out.println("This item was already in the sale. Quantity has been updated.");
            }

            // Display running total
            System.out.println("Total cost (incl VAT): " + formatAmount(runningTotal) + " SEK");
            Amount totalVAT = controller.getCurrentSale().calculateTotalVat();
            System.out.println("Total VAT : " + formatAmount(totalVAT) + " SEK");
            System.out.println();

        } else {
            System.out.println("Item with ID " + itemID + " not found in inventory!");
            System.out.println();
        }
    }

    private String formatAmount(Amount amount) {
        // Format with colon as decimal separator as per requirements example
        return String.format("%.2f", amount.getValue().doubleValue()).replace('.', ':');
    }

}
