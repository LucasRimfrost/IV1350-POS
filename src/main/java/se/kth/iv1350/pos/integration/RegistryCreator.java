package se.kth.iv1350.pos.integration;

/**
 * This class creates all registry classes, providing a single access point
 * to all external systems.
 */
public class RegistryCreator {
    private final ItemRegistry itemRegistry;
    private final DiscountRegistry discountRegistry;

    /**
     * Creates a new instance and initializes all needed registry classes.
     */
    public RegistryCreator() {
        itemRegistry = new ItemRegistry();
        discountRegistry = new DiscountRegistry();
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
}
