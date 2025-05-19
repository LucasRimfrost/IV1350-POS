package se.kth.iv1350.pos.controller;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.kth.iv1350.pos.exception.DatabaseConnectionException;
import se.kth.iv1350.pos.exception.ItemNotFoundException;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.util.Amount;
import java.math.BigDecimal;

/**
 * Tests the Controller class, which coordinates all operations in the
 * Point-of-Sale system.
 */
public class ControllerTest {
    private Controller controller;

    /**
     * Sets up a new controller before each test.
     */
    @Before
    public void setUp() {
        // Use getInstance() instead of constructor since RegistryCreator is now singleton
        RegistryCreator creator = RegistryCreator.getInstance();
        controller = new Controller(creator);
    }

    /**
     * Cleans up after each test.
     */
    @After
    public void tearDown() {
        controller = null;
    }

    /**
     * Tests starting a new sale.
     */
    @Test
    public void testStartNewSale() {
        controller.startNewSale();
        assertNotNull("Sale should be created", controller.getCurrentSale());
    }

    /**
     * Tests entering a valid item.
     */
    @Test
    public void testEnterValidItem() {
        controller.startNewSale();
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);

        assertNotNull("Result should not be null for valid item", result);
        assertEquals("1", result.getItem().getItemID());
        assertFalse("First entry should not be a duplicate", result.isDuplicate());
    }

    /**
     * Tests entering an invalid item.
     */
    @Test
    public void testEnterInvalidItem() {
        controller.startNewSale();
        try {
            controller.enterItem("non-existent", 1);
            fail("Should throw OperationFailedException for non-existent item");
        } catch (OperationFailedException e) {
            // Expected exception, test passed
            assertTrue("Exception cause should be ItemNotFoundException",
                       e.getCause() instanceof ItemNotFoundException);
        }
    }

    /**
     * Tests database connection failure exception.
     */
    @Test
    public void testDatabaseConnectionFailure() {
        controller.startNewSale();
        try {
            controller.enterItem("999", 1);
            fail("Should throw OperationFailedException for database connection failure");
        } catch (OperationFailedException e) {
            // Expected exception, test passed
            assertTrue("Exception cause should be DatabaseConnectionException",
                       e.getCause() instanceof DatabaseConnectionException);
        }
    }

    /**
     * Tests entering the same item twice to verify duplicate detection.
     */
    @Test
    public void testEnterDuplicateItem() {
        controller.startNewSale();
        controller.enterItem("1", 1);
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);

        assertTrue("Second entry of same item should be marked as duplicate", result.isDuplicate());
    }

    /**
     * Tests that running total is calculated correctly.
     */
    @Test
    public void testRunningTotal() {
        controller.startNewSale();
        controller.enterItem("1", 1); // Price 10.0 + VAT 1.2 = 11.2

        Controller.ItemWithRunningTotal result = controller.enterItem("2", 1); // Price 15.0 + VAT 1.8 = 16.8

        // Total should be 11.2 + 16.8 = 28.0
        BigDecimal expectedTotal = new BigDecimal("28.0").setScale(2);
        BigDecimal actualTotal = result.getRunningTotal().getValue();

        assertEquals("Running total should be calculated correctly",
                     0, expectedTotal.compareTo(actualTotal));
    }

    /**
     * Tests ending a sale and getting the final total.
     */
    @Test
    public void testEndSale() {
        controller.startNewSale();
        controller.enterItem("1", 1); // Price 10.0 + VAT 1.2 = 11.2
        controller.enterItem("2", 1); // Price 15.0 + VAT 1.8 = 16.8

        Amount total = controller.endSale();

        assertNotNull("Total should not be null", total);
        // Total should be 11.2 + 16.8 = 28.0
        BigDecimal expectedTotal = new BigDecimal("28.0").setScale(2);

        assertEquals("End sale should return correct total",
                     0, expectedTotal.compareTo(total.getValue()));
    }

    /**
     * Tests processing a payment and calculating change.
     */
    @Test
    public void testProcessPayment() {
        controller.startNewSale();
        controller.enterItem("1", 1); // Price 10.0 + VAT 1.2 = 11.2
        Amount total = controller.endSale();

        Amount payment = new Amount(20.0);
        Amount change = controller.processPayment(payment);

        assertNotNull("Change should not be null", change);

        // Expected change: 20.0 - 11.2 = 8.8
        BigDecimal expectedChange = payment.getValue().subtract(total.getValue());
        assertEquals("Change should be calculated correctly",
                0, expectedChange.compareTo(change.getValue()));
    }

    /**
     * Tests getting total VAT.
     */
    @Test
    public void testGetTotalVAT() {
        controller.startNewSale();
        controller.enterItem("1", 1); // VAT 1.2
        controller.enterItem("2", 1); // VAT 1.8

        Amount vatAmount = controller.getCurrentTotalVAT();

        assertNotNull("VAT amount should not be null", vatAmount);

        // Total VAT should be 1.2 + 1.8 = 3.0
        BigDecimal expectedVAT = new BigDecimal("3.0").setScale(2);
        assertEquals("Total VAT should be calculated correctly",
                     0, expectedVAT.compareTo(vatAmount.getValue()));
    }

    /**
     * Tests entering item when no sale has been started.
     */
    @Test
    public void testEnterItemWithoutStartingSale() {
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);
        assertNull("Result should be null when no sale has been started", result);
    }

    /**
     * Tests ending a sale when no sale has been started.
     */
    @Test
    public void testEndSaleWithoutStartingSale() {
        Amount total = controller.endSale();
        assertNull("Total should be null when no sale has been started", total);
    }

    /**
     * Tests processing payment when no sale has been started.
     */
    @Test
    public void testProcessPaymentWithoutStartingSale() {
        Amount change = controller.processPayment(new Amount(100.0));
        assertNull("Change should be null when no sale has been started", change);
    }

    /**
     * Tests that appropriate exception is thrown when looking for a non-existent item.
     */
    @Test
    public void testEnterNonExistentItemException() {
        controller.startNewSale();

        try {
            controller.enterItem("non-existent-id", 1);
            fail("Should throw OperationFailedException for non-existent item");
        } catch (OperationFailedException e) {
            // Verify the cause is ItemNotFoundException
            assertTrue("Cause should be ItemNotFoundException",
                e.getCause() instanceof ItemNotFoundException);

            // Verify the error message is correct
            assertTrue("Error message should be informative",
                e.getMessage().contains("Item not found"));

            // Verify that original exception info is preserved
            ItemNotFoundException cause = (ItemNotFoundException) e.getCause();
            assertEquals("Original exception should contain correct item ID",
                "non-existent-id", cause.getItemID());
        }
    }

    /**
     * Tests that appropriate exception is thrown when database connection fails.
     */
    @Test
    public void testDatabaseConnectionFailureException() {
        controller.startNewSale();

        try {
            // "999" is the special item ID that triggers database connection failure
            controller.enterItem("999", 1);
            fail("Should throw OperationFailedException for database connection failure");
        } catch (OperationFailedException e) {
            // Verify the cause is DatabaseConnectionException
            assertTrue("Cause should be DatabaseConnectionException",
                e.getCause() instanceof DatabaseConnectionException);

            // Verify the error message is correct
            assertTrue("Error message should mention database",
                e.getMessage().contains("Database unavailable"));
        }
    }

    /**
     * Tests that controller state doesn't change when exception is thrown.
     */
    @Test
    public void testNoStateChangeWhenExceptionThrown() {
        // Start a new sale
        controller.startNewSale();

        // Add a valid item first
        controller.enterItem("1", 1);

        // Store current sale state
        int initialItemCount = controller.getCurrentSale().getItems().size();

        try {
            // Try to enter a non-existent item, which will throw an exception
            controller.enterItem("non-existent-id", 1);
            fail("Should throw OperationFailedException");
        } catch (OperationFailedException e) {
            // Expected exception, now verify state hasn't changed
            assertEquals("Sale item count should not change after exception",
                initialItemCount, controller.getCurrentSale().getItems().size());
        }
    }
}
