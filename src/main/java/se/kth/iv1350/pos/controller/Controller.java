package se.kth.iv1350.pos.controller;

import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.CustomerDTO;
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
import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;
import se.kth.iv1350.pos.util.ErrorLogger;

/**
 * This is the application's controller. All calls to the model and integration
 * layers pass through this class. It coordinates the processes of the Point-of-Sale
 * system.
 */
public class Controller {
    // External systems and registries
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final Printer printer;
    private final AccountingSystem accountingSystem;

    // Internal components
    private final CashRegister cashRegister;
    private final List<SaleObserver> saleObservers;
    private final ErrorLogger errorLogger;

    // Current transaction
    private Sale currentSale;

    /**
     * Creates a new instance.
     *
     * @param creator Used to get all classes that handle database calls.
     */
    public Controller(RegistryCreator creator) {
        // Initialize external system connections
        this.itemRegistry = creator.getItemRegistry();
        this.discountRegistry = creator.getDiscountRegistry();
        this.printer = creator.getPrinter();
        this.accountingSystem = creator.getAccountingSystem();

        // Initialize internal components
        this.cashRegister = new CashRegister();
        this.saleObservers = new ArrayList<>();
        this.errorLogger = new ErrorLogger();
    }

    /**
     * Adds an observer that will be notified of sale events.
     * The observer will be notified when a sale is completed with payment.
     *
     * @param observer The observer to add.
     */
    public void addSaleObserver(SaleObserver observer) {
        saleObservers.add(observer);
        if (currentSale != null) {
            currentSale.addSaleObserver(observer);
        }
    }

    /**
     * Starts a new sale. This method must be called before doing anything else with the sale.
     * Initializes a new Sale object to manage the current transaction and registers observers.
     */
    public void startNewSale() {
        currentSale = new Sale();

        // Register all observers with the new sale
        for (SaleObserver observer : saleObservers) {
            currentSale.addSaleObserver(observer);
        }
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
        // Check if sale has been started
        if (currentSale == null) {
            return null; // No sale started
        }

        try {
            // Find the item in inventory
            ItemDTO item = itemRegistry.findItem(itemID);

            // Check if this item is already in the sale
            boolean isDuplicate = isItemAlreadyInSale(itemID);

            // Add the item to the sale
            currentSale.addItem(item, quantity);

            // Return information about the entered item and updated totals
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
     * Checks if an item with the specified ID is already in the current sale.
     *
     * @param itemID The item identifier to check
     * @return true if the item is already in the sale, false otherwise
     */
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
            return null; // No sale in progress
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
            return null; // No sale in progress
        }

        // Process the payment and calculate change
        CashPayment payment = new CashPayment(paidAmount);
        Amount change = currentSale.pay(payment);

        // Complete all transaction-related operations
        completeTransaction(payment);

        return change;
    }

    /**
     * Completes all operations related to finalizing a transaction after payment.
     *
     * @param payment The payment that was made
     */
    private void completeTransaction(CashPayment payment) {
        if (currentSale == null || payment == null) {
            return; // Safety check
        }

        // Update cash register
        cashRegister.addPayment(payment);

        // Update accounting records
        updateAccountingRecords();

        // Update inventory
        try {
            currentSale.updateInventory(itemRegistry);
        } catch (Exception e) {
            errorLogger.logException(e);
            // Continue with transaction completion even if inventory update fails
        }

        // Print receipt
        currentSale.printReceipt(printer);
    }

    /**
     * Updates accounting records with the sale information.
     */
    private void updateAccountingRecords() {
        if (currentSale == null || accountingSystem == null) {
            return; // Safety check
        }

        try {
            accountingSystem.recordSale(currentSale);
            accountingSystem.updateSalesStatistics(currentSale.calculateTotalWithVat());
        } catch (Exception e) {
            errorLogger.logException(e);
            // Continue with transaction completion even if accounting update fails
        }
    }

    /**
     * Requests a discount for the current sale based on customer ID.
     * Applies any eligible discounts to the current sale.
     *
     * @param customerID The ID of the customer requesting the discount.
     * @return The total price after the discount, or null if no sale is in progress.
     */
    public Amount requestDiscount(String customerID) {
        if (currentSale == null) {
            return null; // No sale in progress
        }

        // Create customer DTO and calculate applicable discount
        CustomerDTO customer = new CustomerDTO(customerID);
        Amount discount = calculateDiscountForCustomer(customerID);

        // Apply the discount to the sale
        return currentSale.applyDiscount(customer, discount);
    }

    /**
     * Calculates the applicable discount for a customer.
     *
     * @param customerID The customer's ID
     * @return The calculated discount amount
     */
    private Amount calculateDiscountForCustomer(String customerID) {
        if (currentSale == null || discountRegistry == null || customerID == null) {
            return new Amount(0); // Safety check, return zero discount
        }

        try {
            return discountRegistry.getDiscount(
                currentSale.getItems(),
                currentSale.calculateTotalWithVat(),
                customerID
            );
        } catch (Exception e) {
            errorLogger.logException(e);
            return new Amount(0); // Return zero discount if calculation fails
        }
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
            return null; // No sale in progress
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
}
