package se.kth.iv1350.pos.integration;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
import se.kth.iv1350.pos.dto.ItemDTO;

/**
 * Tests the ItemRegistry class, focusing on exception handling.
 */
public class ItemRegistryTest {
    private ItemRegistry itemRegistry;

    @Before
    public void setUp() {
        itemRegistry = new ItemRegistry();
    }

    @After
    public void tearDown() {
        itemRegistry = null;
    }

    /**
     * Tests that ItemNotFoundException is thrown when looking for a non-existent item.
     */
    @Test
    public void testFindNonExistentItem() {
        try {
            itemRegistry.findItem("non-existent-id");
            fail("Should throw ItemNotFoundException for non-existent item");
        } catch (ItemNotFoundException e) {
            // Test exception contains correct information
            assertEquals("Exception should contain correct item ID",
                         "non-existent-id", e.getItemID());
            assertTrue("Exception message should mention item ID",
                      e.getMessage().contains("non-existent-id"));
        } catch (DatabaseConnectionException e) {
            fail("Wrong exception type thrown: " + e);
        }
    }

    /**
     * Tests that DatabaseConnectionException is thrown when database connection fails.
     */
    @Test
    public void testDatabaseConnectionFailure() {
        try {
            // "999" is the special item ID that triggers database connection failure
            itemRegistry.findItem("999");
            fail("Should throw DatabaseConnectionException for item ID 999");
        } catch (ItemNotFoundException e) {
            fail("Wrong exception type thrown: " + e);
        } catch (DatabaseConnectionException e) {
            // Test exception contains correct information
            assertTrue("Exception message should mention database connection",
                       e.getMessage().contains("connect to inventory database"));
        }
    }

    /**
     * Tests that object state doesn't change when exception is thrown.
     * Uses indirect method since items collection is private.
     */
    @Test
    public void testNoStateChangeWhenExceptionThrown() {
        // Verify a known item exists before exception
        ItemDTO knownItemBefore = null;
        try {
            knownItemBefore = itemRegistry.findItem("1");
        } catch (Exception e) {
            fail("Should be able to find known item: " + e.getMessage());
        }

        // Now try to find a non-existent item, which will throw an exception
        try {
            itemRegistry.findItem("non-existent-id");
            fail("Should throw ItemNotFoundException");
        } catch (ItemNotFoundException | DatabaseConnectionException e) {
            // Expected exception
        }

        // Verify the known item can still be found and has the same properties
        // This indirectly proves state hasn't changed
        try {
            ItemDTO knownItemAfter = itemRegistry.findItem("1");
            assertEquals("Item ID should be the same after exception",
                         knownItemBefore.getItemID(), knownItemAfter.getItemID());
            assertEquals("Item price should be the same after exception",
                         knownItemBefore.getPrice().getValue(),
                         knownItemAfter.getPrice().getValue());
        } catch (Exception e) {
            fail("Should still be able to find known item after exception: " + e.getMessage());
        }

        // Also verify inventory state hasn't changed by checking availability
        boolean availabilityBefore = itemRegistry.checkItemAvailability("1", 1);

        try {
            itemRegistry.findItem("non-existent-id");
            fail("Should throw ItemNotFoundException");
        } catch (ItemNotFoundException | DatabaseConnectionException e) {
            // Expected exception
        }

        boolean availabilityAfter = itemRegistry.checkItemAvailability("1", 1);
        assertEquals("Item availability should not change after exception",
                    availabilityBefore, availabilityAfter);
    }

    /**
     * Tests that valid items can be found successfully.
     */
    @Test
    public void testFindValidItem() {
        try {
            ItemDTO item = itemRegistry.findItem("1");
            assertNotNull("Should find valid item", item);
            assertEquals("Item should have correct ID", "1", item.getItemID());
            assertEquals("Item should have correct name", "Kellogg's Cornflakes", item.getName());
        } catch (Exception e) {
            fail("Should not throw exception for valid item: " + e.getMessage());
        }
    }

    /**
     * Tests that inventory check works correctly.
     */
    @Test
    public void testCheckItemAvailability() {
        // Valid item with sufficient quantity
        boolean available = itemRegistry.checkItemAvailability("1", 1);
        assertTrue("Valid item with sufficient quantity should be available", available);

        // Valid item with excessive quantity
        available = itemRegistry.checkItemAvailability("1", 100);
        assertFalse("Valid item with excessive quantity should not be available", available);

        // Non-existent item
        available = itemRegistry.checkItemAvailability("non-existent-id", 1);
        assertFalse("Non-existent item should not be available", available);
    }
}
