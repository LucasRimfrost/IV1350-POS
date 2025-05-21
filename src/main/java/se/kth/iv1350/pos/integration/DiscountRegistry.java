package se.kth.iv1350.pos.integration;

/**
 * Stub implementation for the discount registry.
 */
public class DiscountRegistry {
    private static final DiscountRegistry instance = new DiscountRegistry();

    /**
     * Gets the singleton instance of the DiscountRegistry.
     *
     * @return The singleton instance.
     */
    public static DiscountRegistry getInstance() {
        return instance;
    }

    private DiscountRegistry() {
    }
}
