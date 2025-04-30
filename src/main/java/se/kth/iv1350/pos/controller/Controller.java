package se.kth.iv1350.pos.controller;

import se.kth.iv1350.pos.dto.CustomerDTO;
import se.kth.iv1350.pos.dto.ItemDTO;
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
 * layers pass through this class.
 */
public class Controller {
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final Printer printer;
    private final CashRegister cashRegister;

    private Sale currentSale;

    /**
     * Creates a new instance.
     *
     * @param creator Used to get all classes that handle database calls.
     * @param printer The printer that will be used to print receipts.
     */
    public Controller(RegistryCreator creator, Printer printer) {
        this.itemRegistry = creator.getItemRegistry();
        this.discountRegistry = creator.getDiscountRegistry();
        this.printer = printer;
        this.cashRegister = new CashRegister();
    }

    /**
     * Starts a new sale. This method must be called before doing anything else with the sale.
     */
    public void startNewSale() {
        currentSale = new Sale();
    }

    /**
     * Enters an item into the current sale.
     *
     * @param itemID The identifier of the item that is being entered.
     * @param quantity The quantity of the specified item.
     * @return Information about the entered item, including price and description.
     *         Returns null if the item does not exist.
     */
    public ItemDTO enterItem(String itemID, int quantity) {
        ItemDTO item = itemRegistry.findItem(itemID);
        if (item == null) {
            return null;
        }

        SaleLineItem lineItem = currentSale.addItem(item, quantity);
        return item;
    }

    /**
     * Ends the sale. No more items can be entered after this method is called.
     *
     * @return The total price of the sale, including VAT.
     */
    public Amount endSale() {
        return currentSale.calculateTotalWithVat();
    }

    /**
     * Handles payment for the current sale.
     *
     * @param paidAmount The paid amount.
     */
    public void pay(Amount paidAmount) {
        CashPayment payment = new CashPayment(paidAmount);
        currentSale.pay(payment);
        cashRegister.addPayment(payment);

        // Update inventory
        for (SaleLineItem item : currentSale.getItems()) {
            itemRegistry.updateInventory(item.getItem().getItemID(), item.getQuantity());
        }

        // Print receipt
        currentSale.printReceipt(printer);
    }

    /**
     * Requests a discount for the current sale.
     *
     * @param customerID The ID of the customer requesting the discount.
     * @return The total price after the discount.
     */
    public Amount requestDiscount(String customerID) {
        CustomerDTO customer = new CustomerDTO(customerID);
        Amount discount = discountRegistry.getDiscount(
            currentSale.getItems(),
            currentSale.calculateTotalWithVat(),
            customerID
        );
        return currentSale.applyDiscount(customer, discount);
    }
}
