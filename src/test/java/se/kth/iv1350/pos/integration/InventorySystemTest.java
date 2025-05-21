package se.kth.iv1350.pos.integration;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the InventorySystem class, which is responsible for inventory management.
 */
public class InventorySystemTest {
    private InventorySystem inventorySystem;
    private ItemRegistry itemRegistry;
    private List<SaleLineItem> testItems;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        // Create a real ItemRegistry for most tests
        itemRegistry = new ItemRegistry();
        inventorySystem = new InventorySystem(itemRegistry);

        // Set up test items
        testItems = new ArrayList<>();
        ItemDTO itemDTO1 = new ItemDTO("1", "Test Item 1", "Description 1", new Amount(10.0), 0.25);
        ItemDTO itemDTO2 = new ItemDTO("2", "Test Item 2", "Description 2", new Amount(20.0), 0.12);
        testItems.add(new SaleLineItem(itemDTO1, 2));
        testItems.add(new SaleLineItem(itemDTO2, 3));

        // Set up capture of System.out
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        inventorySystem = null;
        itemRegistry = null;
        testItems = null;
        System.setOut(originalOut);
    }

    /**
     * Tests updating inventory with valid items and quantities.
     */
    @Test
    public void testUpdateInventory() {
        boolean result = inventorySystem.updateInventory(testItems);

        assertTrue("Inventory update should succeed with valid items", result);

        // Verify output contains expected messages
        String output = outContent.toString();
        assertTrue("Output should indicate update was performed",
                  output.contains("Updating inventory"));
        assertTrue("Output should indicate successful update",
                  output.contains("successfully updated"));
    }

    /**
     * Tests updating inventory with an empty list.
     */
    @Test
    public void testUpdateInventoryWithEmptyList() {
        boolean result = inventorySystem.updateInventory(new ArrayList<>());

        assertTrue("Inventory update should succeed with empty list", result);

        // Verify output
        String output = outContent.toString();
        assertTrue("Output should indicate update was performed",
                  output.contains("Updating inventory"));
        assertTrue("Output should indicate successful update",
                  output.contains("successfully updated"));
    }

    /**
     * Tests updating inventory when ItemRegistry fails to update some items.
     */
    @Test
    public void testUpdateInventoryWithFailure() {
        // Create a mock ItemRegistry that fails updates
        class MockItemRegistry extends ItemRegistry {
            @Override
            boolean decreaseInventoryQuantity(String itemID, int quantity) {
                return false; // Always fail
            }
        }

        MockItemRegistry mockRegistry = new MockItemRegistry();
        InventorySystem system = new InventorySystem(mockRegistry);

        boolean result = system.updateInventory(testItems);

        assertFalse("Inventory update should fail when registry fails", result);

        // Verify output
        String output = outContent.toString();
        assertTrue("Output should indicate update was performed",
                  output.contains("Updating inventory"));
        assertTrue("Output should indicate failed update",
                  output.contains("failed") && !output.contains("successfully updated"));
    }

    /**
     * Tests updating inventory with mixed success/failure.
     */
    @Test
    public void testUpdateInventoryWithMixedResults() {
        // Create a mock ItemRegistry that selectively fails updates
        class MockItemRegistry extends ItemRegistry {
            @Override
            boolean decreaseInventoryQuantity(String itemID, int quantity) {
                return "1".equals(itemID); // Succeed for item 1, fail for item 2
            }
        }

        MockItemRegistry mockRegistry = new MockItemRegistry();
        InventorySystem system = new InventorySystem(mockRegistry);

        boolean result = system.updateInventory(testItems);

        assertFalse("Inventory update should fail when any item fails", result);

        // Verify output
        String output = outContent.toString();
        assertTrue("Output should indicate update was performed",
                  output.contains("Updating inventory"));
        assertTrue("Output should indicate failed update",
                  output.contains("failed") && !output.contains("successfully updated"));
    }

    /**
     * Tests updating inventory with all valid items but excessive quantities.
     */
    @Test
    public void testUpdateInventoryWithExcessiveQuantities() {
        // Create items with excessive quantities
        List<SaleLineItem> largeQuantityItems = new ArrayList<>();
        ItemDTO itemDTO = new ItemDTO("1", "Test Item", "Description", new Amount(10.0), 0.25);
        largeQuantityItems.add(new SaleLineItem(itemDTO, 100)); // More than the 50 in stock

        boolean result = inventorySystem.updateInventory(largeQuantityItems);

        assertFalse("Inventory update should fail with excessive quantities", result);

        // Verify output
        String output = outContent.toString();
        assertTrue("Output should indicate update was performed",
                  output.contains("Updating inventory"));
        assertTrue("Output should indicate failed update",
                  output.contains("failed") && !output.contains("successfully updated"));
    }
}
