package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.ItemRegistrationDTO;
import se.kth.iv1350.pos.dto.PaymentDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.exception.OperationFailedException;
import se.kth.iv1350.pos.util.Amount;

/**
 * This class represents the view of the POS system.
 * It contains hardcoded execution for simulation.
 */
public class View {
    private final Controller controller;

    /**
     * Creates a new instance.
     *
     * @param controller The controller to use
     */
    public View(Controller controller) {
        this.controller = controller;
    }

    /**
     * Simulates a user interaction with the system.
     */
    public void runFakeExecution() {
        startNewSale();
        registerItems();
        completeSale();
        processPayment();
    }

    private void startNewSale() {
        printDivider("Starting New Sale");
        controller.startNewSale();
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

        printActionHeader("Add 1 item with non-existent item id 999:");
        scanItem("999", 1);

        printActionHeader("Add 1 item with item id that triggers database error 9999:");
        scanItem("9999", 1);
    }

    private void completeSale() {
        printActionHeader("End sale:");

        try {
            SaleDTO saleDTO = controller.endSale();
            System.out.println("Total cost (incl VAT): " + formatAmount(saleDTO.totalWithVat()) + " SEK");
        } catch (OperationFailedException e) {
            printErrorMessage(e);
        }
    }

    private void processPayment() {
        Amount paymentAmount = new Amount(100);
        printActionHeader("Customer pays " + paymentAmount + ":");

        try {
            PaymentDTO paymentResult = controller.processPayment(paymentAmount);
            System.out.println("\nChange to give the customer: " +
                formatAmount(paymentResult.changeAmount()) + " SEK");
        } catch (OperationFailedException e) {
            printErrorMessage(e);
        }
    }

    private void scanItem(String itemID, int quantity) {
        try {
            ItemRegistrationDTO result = controller.enterItem(itemID, quantity);

            displayItemInfo(result.item());

            if (result.isDuplicate()) {
                System.out.println("This item was already in the sale. Quantity has been updated.");
            }

            displayRunningTotal(result.runningTotal(), result.runningVat());

        } catch (OperationFailedException e) {
            printErrorMessage(e);
        }
    }

    private void printErrorMessage(OperationFailedException e) {
        System.out.println("ERROR: " + e.getMessage());

        if (e.getCause() != null) {
            System.out.println("Technical details: " + e.getCause().getMessage());
        }

        System.out.println();
    }

    private void displayItemInfo(ItemDTO item) {
        System.out.println("Item ID : " + item.itemID());
        System.out.println("Item name : " + item.name());
        System.out.println("Item cost : " + formatAmount(item.price()) + " SEK");
        System.out.println("VAT : " + (int)(item.vatRate() * 100) + "%");
        System.out.println("Item description : " + item.description());
        System.out.println();
    }

    private void displayRunningTotal(Amount total, Amount totalVAT) {
        System.out.println("Total cost (incl VAT): " + formatAmount(total) + " SEK");
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
        System.out.println("\n" + title);
        System.out.println("----------------------------------------");
    }
}
