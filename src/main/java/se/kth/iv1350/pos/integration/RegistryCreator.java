package se.kth.iv1350.pos.integration;

/**
 * This class creates all registry classes, providing a single access point
 * to all external systems. This class is implemented as a singleton.
 */
public class RegistryCreator {
    private static RegistryCreator instance = new RegistryCreator();

    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final AccountingSystem accountingSystem;
    private final Printer printer;
    private final InventorySystem inventorySystem;

    /**
     * Gets the singleton instance of the RegistryCreator.
     *
     * @return The singleton instance.
     */
    public static RegistryCreator getInstance() {
        return instance;
    }

    /**
     * Gets the item registry.
     *
     * @return The item registry.
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Gets the discount registry.
     *
     * @return The discount registry.
     */
    public DiscountRegistry getDiscountRegistry() {
        return discountRegistry;
    }

    /**
     * Gets the accounting system interface.
     *
     * @return The accounting system interface.
     */
    public AccountingSystem getAccountingSystem() {
        return accountingSystem;
    }

    /**
     * Gets the printer interface.
     *
     * @return The printer interface.
     */
    public Printer getPrinter() {
        return printer;
    }

    /**
     * Gets the inventory system interface.
     *
     * @return The inventory system interface.
     */
    public InventorySystem getInventorySystem() {
        return inventorySystem;
    }

    private RegistryCreator() {
        itemRegistry = ItemRegistry.getInstance();
        discountRegistry = DiscountRegistry.getInstance();
        accountingSystem = AccountingSystem.getInstance();
        printer = Printer.getInstance();
        inventorySystem = InventorySystem.getInstance();
    }
}
