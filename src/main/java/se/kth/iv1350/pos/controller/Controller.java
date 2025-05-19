package se.kth.iv1350.pos.controller;

import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.AccountingSystem;
import se.kth.iv1350.pos.integration.DiscountRegistry;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
import se.kth.iv1350.pos.model.CashPayment;
import se.kth.iv1350.pos.model.CashRegister;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;
import se.kth.iv1350.pos.util.ErrorLogger;

/**
 * This is the application's controller. All calls to the model and integration
 * layers pass through this class. It coordinates the processes of the Point-of-Sale
 * system.
 */
public class Controller {
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final Printer printer;
    private final AccountingSystem accountingSystem;

    private final CashRegister cashRegister;
    private final ErrorLogger errorLogger;

    private Sale currentSale;

    /**
     * Creates a new instance.
     *
     * @param creator Used to get all classes that handle database calls.
     */
    public Controller(RegistryCreator creator) {
        this.itemRegistry = creator.getItemRegistry();
        this.discountRegistry = creator.getDiscountRegistry();
        this.printer = creator.getPrinter();
        this.accountingSystem = creator.getAccountingSystem();

        this.cashRegister = new CashRegister();
        this.errorLogger = new ErrorLogger();
    }

    /**
     * Starts a new sale. This method must be called before doing anything else with the sale.
     * Initializes a new Sale object to manage the current transaction and registers observers.
     */
    public void startNewSale() {
        currentSale = new Sale();
    }

    /**
     * Enters an item into the current sale. Handles both new items and duplicate entries.
     * For duplicate entries, the quantity will be added to the existing item.
     *
     * @param itemID The identifier of the item that is being entered.
     * @param quantity The quantity of the specified item.
     * @return Information about the entered item, including price and description.
     *         Returns null if the item does not exist or no sale has been started.
     * @throws OperationFailedException If the item cannot be found or if there's a database connection issue.
     */
    public ItemWithRunningTotal enterItem(String itemID, int quantity) throws OperationFailedException {
        if (currentSale == null) {
            return null;
        }

        try {
            ItemDTO item = itemRegistry.findItem(itemID);
            boolean isDuplicate = isItemAlreadyInSale(itemID);

            currentSale.addItem(item, quantity);

            return new ItemWithRunningTotal(item, currentSale.calculateTotalWithVat(), isDuplicate);
        } catch (ItemNotFoundException exception) {
            errorLogger.logException(exception);
            throw new OperationFailedException("Item not found", exception);
        } catch (DatabaseConnectionException exception) {
            errorLogger.logException(exception);
            throw new OperationFailedException("Database unavailable", exception);
        }
    }

    /**
     * A container for information about an entered item and the running total.
     */
    public static class ItemWithRunningTotal {
        private final ItemDTO item;
        private final Amount runningTotal;
        private final boolean isDuplicate;

        /**
         * Creates a new instance.
         *
         * @param item The entered item
         * @param runningTotal The current running total of the sale
         * @param isDuplicate Whether this item is a duplicate entry
         */
        public ItemWithRunningTotal(ItemDTO item, Amount runningTotal, boolean isDuplicate) {
            this.item = item;
            this.runningTotal = runningTotal;
            this.isDuplicate = isDuplicate;
        }

        /**
         * Gets the entered item.
         *
         * @return The item that was entered
         */
        public ItemDTO getItem() {
            return item;
        }

        /**
         * Gets the running total of the sale after the item was entered.
         *
         * @return The running total of the sale
         */
        public Amount getRunningTotal() {
            return runningTotal;
        }

        /**
         * Checks if this item is a duplicate entry.
         *
         * @return true if this is a duplicate entry, false otherwise
         */
        public boolean isDuplicate() {
            return isDuplicate;
        }
    }

    /**
     * Ends the sale. No more items can be entered after this method is called.
     * Calculates the final total including VAT.
     *
     * @return The total price of the sale, including VAT, or null if no sale is in progress.
     */
    public Amount endSale() {
        if (currentSale == null) {
            return null;
        }
        return currentSale.calculateTotalWithVat();
    }

    /**
     * Handles payment for the current sale. Updates inventory, prints receipt,
     * and notifies external systems about the completed sale.
     *
     * @param paidAmount The paid amount.
     * @return The change amount to be given back to the customer, or null if no sale is in progress.
     */
    public Amount processPayment(Amount paidAmount) {
        if (currentSale == null) {
            return null;
        }

        CashPayment payment = new CashPayment(paidAmount);
        Amount change = currentSale.processPayment(payment);

        completeTransaction(payment);

        return change;
    }

    /**
     * Gets the current state of the sale.
     *
     * @return The current sale or null if no sale has been started
     */
    public Sale getCurrentSale() {
        return currentSale;
    }

    /**
     * Gets the current total VAT amount.
     *
     * @return The current total VAT amount, or null if no sale is in progress.
     */
    public Amount getCurrentTotalVAT() {
        if (currentSale == null) {
            return null;
        }
        return currentSale.calculateTotalVat();
    }

    /**
     * Closes all resources held by the controller.
     * Should be called when shutting down the application.
     */
    public void close() {
        try {
            errorLogger.close();
        } catch (Exception e) {
            System.err.println("Error closing controller resources: " + e.getMessage());
        }
    }

    private boolean isItemAlreadyInSale(String itemID) {
        if (currentSale == null) {
            return false;
        }

        for (SaleLineItem lineItem : currentSale.getItems()) {
            if (lineItem.getItem().getItemID().equals(itemID)) {
                return true;
            }
        }
        return false;
    }

    private void completeTransaction(CashPayment payment) {
        if (currentSale == null || payment == null) {
            return;
        }

        cashRegister.addPayment(payment);

        updateAccountingRecords();

        try {
            currentSale.updateInventory(itemRegistry);
        } catch (Exception e) {
            errorLogger.logException(e);
        }

        currentSale.printReceipt(printer);
    }

    private void updateAccountingRecords() {
        if (currentSale == null || accountingSystem == null) {
            return;
        }

        try {
            accountingSystem.recordSale(currentSale);
            accountingSystem.updateSalesStatistics(currentSale.calculateTotalWithVat());
        } catch (Exception e) {
            errorLogger.logException(e);
        }
    }
}
