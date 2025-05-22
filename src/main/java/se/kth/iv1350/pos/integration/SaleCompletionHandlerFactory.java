package se.kth.iv1350.pos.integration;

import java.util.Arrays;
import java.util.List;

/**
 * Factory class implementing the GoF Factory Method design pattern for creating
 * sale completion handlers. This factory encapsulates the creation logic for
 * different types of handlers that process completed sales with external systems.
 *
 * The Factory pattern promotes loose coupling by eliminating the need for
 * client code to know the specific classes of handlers being created.
 * This makes the system easier to extend with new handler types.
 *
 * This is part of the GoF Factory Method pattern implementation.
 */
public class SaleCompletionHandlerFactory {

    /**
     * Factory method that creates all standard handlers needed for sale completion.
     * This method demonstrates the Factory pattern by providing a single point
     * of creation for all sale completion handlers.
     *
     * @param accountingSystem The accounting system for financial record keeping. Must not be null.
     * @param inventorySystem The inventory system for stock management. Must not be null.
     * @return A list containing all standard sale completion handlers.
     */
    public static List<SaleCompletionHandler> createAllHandlers(
            AccountingSystem accountingSystem,
            InventorySystem inventorySystem) {

        return Arrays.asList(
            createAccountingHandler(accountingSystem),
            createInventoryHandler(inventorySystem)
        );
    }

    /**
     * Factory method for creating accounting system handlers.
     * Part of the Factory pattern implementation.
     *
     * @param accountingSystem The accounting system to integrate with. Must not be null.
     * @return A configured accounting handler.
     */
    public static SaleCompletionHandler createAccountingHandler(AccountingSystem accountingSystem) {
        return new AccountingHandler(accountingSystem);
    }

    /**
     * Factory method for creating inventory system handlers.
     * Part of the Factory pattern implementation.
     *
     * @param inventorySystem The inventory system to integrate with. Must not be null.
     * @return A configured inventory handler.
     */
    public static SaleCompletionHandler createInventoryHandler(InventorySystem inventorySystem) {
        return new InventoryHandler(inventorySystem);
    }

    /**
     * Factory method for creating specific types of handlers based on string parameter.
     * This demonstrates the Factory pattern's ability to create objects dynamically
     * based on runtime parameters.
     *
     * @param type The type of handler to create ("accounting" or "inventory").
     * @param accountingSystem The accounting system for accounting handlers.
     * @param inventorySystem The inventory system for inventory handlers.
     * @return A handler of the specified type.
     * @throws IllegalArgumentException if the type is unknown.
     */
    public static SaleCompletionHandler createHandler(String type,
            AccountingSystem accountingSystem,
            InventorySystem inventorySystem) {

        switch (type.toLowerCase()) {
            case "accounting":
                return createAccountingHandler(accountingSystem);
            case "inventory":
                return createInventoryHandler(inventorySystem);
            default:
                throw new IllegalArgumentException("Unknown handler type: " + type);
        }
    }
}
