package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.controller.OperationFailedException;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
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
        initiateSale();
        registerItems();

        completeSale();
        processPayment();

        printDivider("Testing error handling with invalid item");
        scanItem("InvalidItemID", 1);

        printDivider("Testing error handling with database connection failure");
        scanItem("999", 1);
    }

    /**
     * Handles the scanning of an item, displaying appropriate information.
     * Catches and handles exceptions for error cases.
     *
     * @param itemID The identifier of the item being scanned
     * @param quantity The quantity of the item
     */
    public boolean scanItem(String itemID, int quantity) {
        try {
            Controller.ItemWithRunningTotal result = controller.enterItem(itemID, quantity);

            if (result != null) {
                displayItemInfo(result.getItem());

                if (result.isDuplicate()) {
                    System.out.println("This item was already in the sale. Quantity has been updated.");
                }

                displayRunningTotal(result.getRunningTotal());
                return true;
            }
            return false;
        } catch (OperationFailedException e) {
            handleException(e);
            return false;
        }
    }

    private void initiateSale() {
        printDivider("Starting New Sale");
        controller.startNewSale();
        runningTotal = new Amount();
    }

    private void registerItems() {
        printActionHeader("Add 1 item with item id 1:");
        scanItem("1", 1);

        printActionHeader("Add 1 item with item id 1:");
        scanItem("1", 1);

        printActionHeader("Add 1 item with item id 3:");
        scanItem("3", 1);

        printActionHeader("Add 1 item with item id 2:");
        scanItem("2", 1);
    }

    private void completeSale() {
        printActionHeader("End sale:");
        Amount total = controller.endSale();
        System.out.println("Total cost (incl VAT): " + formatAmount(total) + " SEK");
    }

    private void processPayment() {
        Amount paymentAmount = new Amount(100);
        printActionHeader("Customer pays " + paymentAmount + ":");

        Amount change = controller.processPayment(paymentAmount);

        System.out.println("\nChange to give the customer: " + formatAmount(change) + " SEK");
    }

    private void handleException(OperationFailedException e) {
        String errorMsg = "";

        if (e.getCause() instanceof ItemNotFoundException) {
            ItemNotFoundException ex = (ItemNotFoundException) e.getCause();
            errorMsg = "ERROR: Item with ID " + ex.getItemID() + " was not found in inventory!";
        } else if (e.getCause() instanceof DatabaseConnectionException) {
            errorMsg = "ERROR: Could not connect to the database. Please try again later.";
        } else {
            errorMsg = "An error occurred: " + e.getMessage();
        }

        System.out.println(errorMsg);
        System.out.println();
    }

    private void displayItemInfo(ItemDTO item) {
        System.out.println("Item ID : " + item.getItemID());
        System.out.println("Item name : " + item.getName());
        System.out.println("Item cost : " + formatAmount(item.getPrice()) + " SEK");
        System.out.println("VAT : " + (int)(item.getVatRate() * 100) + "%");
        System.out.println("Item description : " + item.getDescription());
        System.out.println();
    }

    private void displayRunningTotal(Amount total) {
        this.runningTotal = total;
        System.out.println("Total cost (incl VAT): " + formatAmount(runningTotal) + " SEK");
        Amount totalVAT = controller.getCurrentTotalVAT();
        System.out.println("Total VAT : " + formatAmount(totalVAT) + " SEK");
        System.out.println();
    }

    private String formatAmount(Amount amount) {
        return String.format("%.2f", amount.getValue().doubleValue()).replace('.', ':');
    }

    private void printActionHeader(String action) {
        System.out.println(action);
    }

    private void printDivider(String title) {
        System.out.println("\n==== " + title + " ====");
    }
}
