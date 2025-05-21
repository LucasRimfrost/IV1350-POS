package se.kth.iv1350.pos.integration;

/**
 * Creates all registry classes, providing a single access point
 * to all external systems.
 */
public class RegistryCreator {
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;
    private final AccountingSystem accountingSystem;
    private final Printer printer;
    private final InventorySystem inventorySystem;

    /**
     * Creates a new instance and initializes all system interfaces.
     */
    public RegistryCreator() {
        itemRegistry = new ItemRegistry();
        discountRegistry = new DiscountRegistry();
        accountingSystem = new AccountingSystem();
        printer = new Printer();
        inventorySystem = new InventorySystem(itemRegistry);
    }

    /**
     * Gets the item registry.
     *
     * @return The item registry
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Gets the discount registry.
     *
     * @return The discount registry
     */
    public DiscountRegistry getDiscountRegistry() {
        return discountRegistry;
    }

    /**
     * Gets the accounting system.
     *
     * @return The accounting system
     */
    public AccountingSystem getAccountingSystem() {
        return accountingSystem;
    }

    /**
     * Gets the printer.
     *
     * @return The printer
     */
    public Printer getPrinter() {
        return printer;
    }

    /**
     * Gets the inventory system.
     *
     * @return The inventory system
     */
    public InventorySystem getInventorySystem() {
        return inventorySystem;
    }
}
