// Controller.java
package se.kth.iv1350.pos.controller;

import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.ItemRegistrationDTO;
import se.kth.iv1350.pos.dto.PaymentDTO;
import se.kth.iv1350.pos.dto.ReceiptDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.integration.AccountingSystem;
import se.kth.iv1350.pos.integration.InventorySystem;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.model.CashPayment;
import se.kth.iv1350.pos.model.CashRegister;
import se.kth.iv1350.pos.model.Receipt;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.model.SaleProcessor;
import se.kth.iv1350.pos.util.Amount;

/**
 * Controller that coordinates operations between the view, model and integration layers.
 * Focus on orchestration with minimal business logic.
 */
public class Controller {
    private final ItemRegistry itemRegistry;
    private final Printer printer;
    private final AccountingSystem accountingSystem;
    private final InventorySystem inventorySystem;

    private final CashRegister cashRegister;
    private final SaleProcessor saleProcessor;

    private Sale currentSale;

    /**
     * Creates a new controller instance with references to external systems.
     *
     * @param creator Used to get all external system handlers
     */
    public Controller(RegistryCreator creator) {
        this.itemRegistry = creator.getItemRegistry();
        this.printer = creator.getPrinter();
        this.accountingSystem = creator.getAccountingSystem();
        this.inventorySystem = creator.getInventorySystem();

        this.cashRegister = new CashRegister();
        this.saleProcessor = new SaleProcessor();
    }

    /**
     * Starts a new sale transaction.
     */
    public void startNewSale() {
        currentSale = new Sale();
    }

    /**
     * Checks if a sale is currently active.
     *
     * @return true if a sale is active, false otherwise
     */
    public boolean isSaleActive() {
        return currentSale != null;
    }

    /**
     * Adds an item to the current sale.
     *
     * @param itemID The identifier of the item to add
     * @param quantity The quantity of the specified item
     * @return Information about the entered item and running total, or null if item not found
     */
    public ItemRegistrationDTO enterItem(String itemID, int quantity) {
        if (!isSaleActive()) {
            return null;
        }

        // Get item information from registry
        ItemDTO item = itemRegistry.findItem(itemID);
        if (item == null) {
            return null;
        }

        // Check if this is a duplicate item
        boolean isDuplicate = false;
        for (var lineItem : currentSale.getItems()) {
            if (lineItem.getItem().itemID().equals(itemID)) {
                isDuplicate = true;
                break;
            }
        }

        // Add item to sale
        currentSale.addItem(item, quantity);

        // Return information about the addition
        return new ItemRegistrationDTO(
            item,
            currentSale.calculateTotalWithVat(),
            currentSale.calculateTotalVat(),
            isDuplicate
        );
    }

    /**
     * Ends the current sale and returns sale information.
     *
     * @return Data about the current sale, or null if no sale is in progress
     */
    public SaleDTO endSale() {
        if (!isSaleActive()) {
            return null;
        }
        return saleProcessor.createSaleDTO(currentSale);
    }

    /**
     * Processes payment for the current sale.
     *
     * @param paidAmount The amount paid by the customer
     * @return Payment information including change, or null if no sale is in progress
     */
    public PaymentDTO processPayment(Amount paidAmount) {
        if (!isSaleActive()) {
            return null;
        }

        CashPayment payment = new CashPayment(paidAmount);
        Amount totalToPay = currentSale.calculateTotalWithVat();
        Amount change = payment.getChange(totalToPay);

        Receipt receipt = currentSale.createReceipt(paidAmount, change);
        ReceiptDTO receiptDTO = saleProcessor.createReceiptDTO(receipt);

        cashRegister.addPayment(payment);
        printer.printReceipt(receiptDTO);
        accountingSystem.recordSale(saleProcessor.createSaleDTO(currentSale));
        inventorySystem.updateInventory(currentSale.getItems());

        return new PaymentDTO(paidAmount, change);
    }

    /**
     * Gets information about the current sale.
     *
     * @return Data about the current sale, or null if no sale is in progress
     */
    public SaleDTO getCurrentSaleInfo() {
        if (!isSaleActive()) {
            return null;
        }
        return saleProcessor.createSaleDTO(currentSale);
    }
}
