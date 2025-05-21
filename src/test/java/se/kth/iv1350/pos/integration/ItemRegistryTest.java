package se.kth.iv1350.pos.integration;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;

/**
 * Tests the ItemRegistry class, which contains all calls to the external inventory system.
 */
public class ItemRegistryTest {
    private ItemRegistry itemRegistry;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        itemRegistry = new ItemRegistry();
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        itemRegistry = null;
    }

    /**
     * Tests finding an existing item.
     */
    @Test
    public void testFindExistingItem() {
        ItemDTO item = itemRegistry.findItem("1"); // Item ID for Cornflakes

        assertNotNull("Should find an existing item", item);
        assertEquals("Item ID should match", "1", item.itemID());
        assertEquals("Item name should match", "Kellogg's Cornflakes", item.name());
    }

    /**
     * Tests finding a non-existent item.
     */
    @Test
    public void testFindNonExistentItem() {
        ItemDTO item = itemRegistry.findItem("999"); // Non-existent item ID

        assertNull("Should not find a non-existent item", item);
    }

    /**
     * Tests checking if an item is available with sufficient quantity.
     */
    @Test
    public void testIsItemAvailableWithSufficientQuantity() {
        boolean available = itemRegistry.isItemAvailable("1", 10); // Initial quantity is 50

        assertTrue("Item should be available with sufficient quantity", available);
    }

    /**
     * Tests checking if an item is available with insufficient quantity.
     */
    @Test
    public void testIsItemAvailableWithInsufficientQuantity() {
        boolean available = itemRegistry.isItemAvailable("1", 100); // Initial quantity is 50

        assertFalse("Item should not be available with insufficient quantity", available);
    }

    /**
     * Tests checking if a non-existent item is available.
     */
    @Test
    public void testIsNonExistentItemAvailable() {
        boolean available = itemRegistry.isItemAvailable("999", 1);

        assertFalse("Non-existent item should not be available", available);
    }

    /**
     * Tests that decreasing inventory quantity works when the item exists
     * and has sufficient quantity.
     */
    @Test
    public void testDecreaseInventoryQuantity() {
        // This is a package-private method, so we need to verify indirectly
        // First check that the item is available
        assertTrue("Item should be available before decrease",
                  itemRegistry.isItemAvailable("1", 10));

        // Then decrease the quantity to the point where it shouldn't be available
        boolean result = itemRegistry.decreaseInventoryQuantity("1", 45); // Initial quantity is 50

        assertTrue("Decrease should succeed", result);
        assertTrue("Item should still be available for small quantity",
                  itemRegistry.isItemAvailable("1", 5));
        assertFalse("Item should not be available for large quantity",
                   itemRegistry.isItemAvailable("1", 10));
    }

    /**
     * Tests decreasing inventory for a non-existent item.
     */
    @Test
    public void testDecreaseInventoryForNonExistentItem() {
        boolean result = itemRegistry.decreaseInventoryQuantity("999", 1);

        assertFalse("Decreasing non-existent item should fail", result);
    }

    /**
     * Tests decreasing inventory by more than available quantity.
     */
    @Test
    public void testDecreaseInventoryBeyondAvailable() {
        boolean result = itemRegistry.decreaseInventoryQuantity("1", 100); // Initial quantity is 50

        assertFalse("Decreasing more than available should fail", result);
        assertTrue("Item quantity should not change after failed decrease",
                  itemRegistry.isItemAvailable("1", 50));
    }

    /**
     * Tests that decreasing by zero quantity works.
     */
    @Test
    public void testDecreaseInventoryByZeroQuantity() {
        boolean result = itemRegistry.decreaseInventoryQuantity("1", 0);

        assertTrue("Decreasing by zero should succeed", result);
        assertTrue("Item quantity should not change after zero decrease",
                  itemRegistry.isItemAvailable("1", 50));
    }
}
