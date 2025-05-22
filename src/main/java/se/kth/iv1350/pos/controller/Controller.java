package se.kth.iv1350.pos.controller;

import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.ItemRegistrationDTO;
import se.kth.iv1350.pos.dto.PaymentDTO;
import se.kth.iv1350.pos.dto.ReceiptDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
import se.kth.iv1350.pos.exception.OperationFailedException;
import se.kth.iv1350.pos.integration.AccountingSystem;
import se.kth.iv1350.pos.integration.DiscountRegistry;
import se.kth.iv1350.pos.integration.InventorySystem;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.integration.SaleCompletionHandler;
import se.kth.iv1350.pos.integration.SaleCompletionHandlerFactory;
import se.kth.iv1350.pos.model.AddItemCommand;
import se.kth.iv1350.pos.model.CashPayment;
import se.kth.iv1350.pos.model.CashRegister;
import se.kth.iv1350.pos.model.ProcessPaymentCommand;
import se.kth.iv1350.pos.model.Receipt;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.model.SaleCommand;
import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.model.SaleProcessor;
import se.kth.iv1350.pos.util.Amount;
import se.kth.iv1350.pos.util.ErrorLogger;

/**
 * The {@code Controller} class is the main coordinator between the
 * view, model, and integration layers of the POS system.
 * It orchestrates the overall sale process and interacts with
 * external systems, such as inventory and accounting.
 */
public class Controller implements AutoCloseable {
    private final ItemRegistry itemRegistry;
    private final Printer printer;
    private final AccountingSystem accountingSystem;
    private final DiscountRegistry discountRegistry;
    private final InventorySystem inventorySystem;
    private final ErrorLogger errorLogger;

    private final CashRegister cashRegister;
    private final SaleProcessor saleProcessor;
    private final List<SaleObserver> saleObservers = new ArrayList<>();
    private final List<AutoCloseable> resourcesToClose = new ArrayList<>();

    private final List<SaleCompletionHandler> saleCompletionHandlers;

    private Sale currentSale;

    /**
     * Constructs a new instance of the controller and initializes it with
     * all necessary external system references.
     *
     * @param creator The registry creator providing access to external system handlers.
     */
    public Controller(RegistryCreator creator) {
        this.itemRegistry = creator.getItemRegistry();
        this.printer = creator.getPrinter();
        this.accountingSystem = creator.getAccountingSystem();
        this.discountRegistry = creator.getDiscountRegistry();
        this.inventorySystem = creator.getInventorySystem();
        this.errorLogger = new ErrorLogger();

        resourcesToClose.add(errorLogger);

        this.cashRegister = new CashRegister();
        this.saleProcessor = new SaleProcessor();

        this.saleCompletionHandlers = SaleCompletionHandlerFactory.createAllHandlers(
            accountingSystem,
            inventorySystem
        );
    }

    /**
     * Registers an observer that will be notified when a sale is completed.
     *
     * @param observer The observer to be registered.
     */
    public void addSaleObserver(SaleObserver observer) {
        saleObservers.add(observer);
        if (observer instanceof AutoCloseable) {
            resourcesToClose.add((AutoCloseable) observer);
        }
    }

    /**
     * Initiates a new sale. Must be called before registering any items.
     */
    public void startNewSale() {
        currentSale = new Sale();
    }

    /**
     * Checks whether a sale is currently in progress.
     *
     * @return {@code true} if a sale is active; {@code false} otherwise.
     */
    public boolean isSaleActive() {
        return currentSale != null;
    }

    /**
     * Registers an item in the current sale using its item ID and quantity.
     * Returns information about the registered item and updated totals.
     *
     * @param itemID   The identifier of the item to register.
     * @param quantity The number of units to register.
     * @return Information about the registered item and sale totals.
     * @throws OperationFailedException If the item is not found or a system error occurs.
     */
    public ItemRegistrationDTO enterItem(String itemID, int quantity) throws OperationFailedException {
        if (!isSaleActive()) {
            throw new OperationFailedException("No active sale. Start a new sale before adding items.");
        }

        try {
            ItemDTO item = itemRegistry.findItem(itemID);
            boolean isDuplicate = false;
            for (var lineItem : currentSale.getItems()) {
                if (lineItem.getItem().itemID().equals(itemID)) {
                    isDuplicate = true;
                    break;
                }
            }

            SaleCommand addItemCommand = new AddItemCommand(currentSale, item, quantity);
            addItemCommand.execute();

            return new ItemRegistrationDTO(
                item,
                currentSale.calculateTotalWithVat(),
                currentSale.calculateTotalVat(),
                isDuplicate
            );

        } catch (ItemNotFoundException e) {
            throw new OperationFailedException("Could not find the specified item: " + itemID, e);
        } catch (DatabaseConnectionException e) {
            errorLogger.logException(e);
            throw new OperationFailedException("Could not perform the operation due to a system error", e);
        }
    }

    /**
     * Finalizes the current sale and returns its summarized data.
     *
     * @return A {@code SaleDTO} containing the finalized sale information.
     * @throws OperationFailedException If there is no active sale.
     */
    public SaleDTO endSale() throws OperationFailedException {
        if (!isSaleActive()) {
            throw new OperationFailedException("No active sale to end.");
        }
        return saleProcessor.createSaleDTO(currentSale);
    }

    /**
     * Processes a cash payment for the current sale, prints a receipt,
     * and uses the Handler + Factory pattern to update external systems.
     *
     * The Factory pattern creates appropriate handlers, and the Handler pattern
     * processes the completed sale with each external system.
     *
     * @param paidAmount The amount paid by the customer.
     * @return A {@code PaymentDTO} containing payment and change information.
     * @throws OperationFailedException If payment processing or system interaction fails.
     */
    public PaymentDTO processPayment(Amount paidAmount) throws OperationFailedException {
        if (!isSaleActive()) {
            throw new OperationFailedException("No active sale to process payment for.");
        }

        try {
            CashPayment payment = new CashPayment(paidAmount);
            Amount totalToPay = currentSale.calculateTotalWithVat();
            Amount change = payment.getChange(totalToPay);

            // Command Pattern: Process payment command
            ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(paidAmount, cashRegister);
            paymentCommand.execute();
            System.out.println("Executed: " + paymentCommand.getDescription());

            Receipt receipt = currentSale.createReceipt(paidAmount, change);
            ReceiptDTO receiptDTO = saleProcessor.createReceiptDTO(receipt);
            printer.printReceipt(receiptDTO);

            // Handler + Factory Pattern: Process completed sale with external systems
            SaleDTO saleDTO = saleProcessor.createSaleDTO(currentSale);
            System.out.println("Using Handler + Factory pattern to process external systems:");
            for (SaleCompletionHandler handler : saleCompletionHandlers) {
                System.out.println("Processing with: " + handler.getHandlerName());
                handler.handle(saleDTO);
            }

            notifyObservers(totalToPay);

            // Clear current sale after completion
            currentSale = null;

            return new PaymentDTO(paidAmount, change);

        } catch (Exception e) {
            errorLogger.logException(e);
            throw new OperationFailedException("Payment processing failed", e);
        }
    }

    /**
     * Returns a summary of the current sale in progress.
     *
     * @return A {@code SaleDTO} representing the current sale's state.
     * @throws OperationFailedException If there is no active sale.
     */
    public SaleDTO getCurrentSaleInfo() throws OperationFailedException {
        if (!isSaleActive()) {
            throw new OperationFailedException("No active sale.");
        }
        return saleProcessor.createSaleDTO(currentSale);
    }

    /**
     * Releases all resources used by the controller.
     * This method is called automatically if the controller is used in a try-with-resources block.
     */
    @Override
    public void close() {
        for (AutoCloseable resource : resourcesToClose) {
            try {
                resource.close();
            } catch (Exception e) {
                System.err.println("Error closing resource: " + e.getMessage());
            }
        }
        System.out.println("Controller resources closed.");
    }

    private void notifyObservers(Amount paidAmount) {
        for (SaleObserver observer : saleObservers) {
            observer.newSale(paidAmount);
        }
    }
}
