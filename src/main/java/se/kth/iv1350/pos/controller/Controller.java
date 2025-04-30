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
import se.kth.iv1350.pos.model.CashPayment;
import se.kth.iv1350.pos.model.CashRegister;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;

/**
 * This is the application's controller. All calls to the model and integration
 * layers pass through this class. It coordinates the processes of the Point-of-Sale
 * system including starting sales, entering items, applying discounts, handling payments,
 * and notifying external systems.
 */
public class Controller {
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final Printer printer;
    private final CashRegister cashRegister;
    private final List<ExternalSystemObserver> externalSystemObservers;
    private final AccountingSystem accountingSystem;

    private Sale currentSale;

    /**
     * Interface for external systems that need to be notified of sale events.
     */
    public interface ExternalSystemObserver {
        /**
         * Called when a sale is completed with payment.
         *
         * @param completedSale The sale that was completed
         */
        void saleCompleted(Sale completedSale);
    }

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
        this.externalSystemObservers = new ArrayList<>();
    }

    /**
     * Adds an observer that will be notified of sale events.
     *
     * @param observer The observer to add
     */
    public void addExternalSystemObserver(ExternalSystemObserver observer) {
        externalSystemObservers.add(observer);
    }

    /**
     * Starts a new sale. This method must be called before doing anything else with the sale.
     * Initializes a new Sale object to manage the current transaction.
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
     *         Returns null if the item does not exist.
     * @throws IllegalStateException if no sale has been started
     */
    public ItemWithRunningTotal enterItem(String itemID, int quantity) {
        if (currentSale == null) {
            throw new IllegalStateException("No sale has been started.");
        }

        ItemDTO item = itemRegistry.findItem(itemID);
        if (item == null) {
            return null;
        }

        boolean isDuplicate = false;
        for (SaleLineItem lineItem : currentSale.getItems()) {
            if (lineItem.getItem().getItemID().equals(itemID)) {
                isDuplicate = true;
                break;
            }
        }

        currentSale.addItem(item, quantity);
        return new ItemWithRunningTotal(item, currentSale.calculateTotalWithVat(), isDuplicate);
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
     * @return The total price of the sale, including VAT.
     * @throws IllegalStateException if no sale has been started
     */
    public Amount endSale() {
        if (currentSale == null) {
            throw new IllegalStateException("No sale has been started.");
        }
        return currentSale.calculateTotalWithVat();
    }

    /**
     * Handles payment for the current sale. Updates inventory, prints receipt,
     * and notifies external systems about the completed sale.
     *
     * @param paidAmount The paid amount.
     * @return The change amount to be given back to the customer.
     * @throws IllegalStateException if no sale has been started
     */
    public Amount pay(Amount paidAmount) {
        if (currentSale == null) {
            throw new IllegalStateException("No sale has been started.");
        }

        CashPayment payment = new CashPayment(paidAmount);
        Amount change = currentSale.pay(payment);
        cashRegister.addPayment(payment);

        // Notify accounting system
        accountingSystem.recordSale(currentSale);
        accountingSystem.updateSalesStatistics(currentSale.calculateTotalWithVat());

        // Update inventory
        currentSale.updateInventory(itemRegistry);

        // Print receipt
        currentSale.printReceipt(printer);

        // Notify external systems
        notifyExternalSystems();

        return change;
    }

    /**
     * Notifies all registered external systems about the completed sale.
     */
    private void notifyExternalSystems() {
        for (ExternalSystemObserver observer : externalSystemObservers) {
            observer.saleCompleted(currentSale);
        }
    }

    /**
     * Requests a discount for the current sale based on customer ID.
     * Applies any eligible discounts to the current sale.
     *
     * @param customerID The ID of the customer requesting the discount.
     * @return The total price after the discount.
     * @throws IllegalStateException if no sale has been started
     */
    public Amount requestDiscount(String customerID) {
        if (currentSale == null) {
            throw new IllegalStateException("No sale has been started.");
        }
        
        CustomerDTO customer = new CustomerDTO(customerID);
        Amount discount = discountRegistry.getDiscount(
            currentSale.getItems(),
            currentSale.calculateTotalWithVat(),
            customerID
        );
        return currentSale.applyDiscount(customer, discount);
    }

    /**
     * Gets the current state of the sale.
     *
     * @return The current sale or null if no sale has been started
     */
    public Sale getCurrentSale() {
        return currentSale;
    }
}
