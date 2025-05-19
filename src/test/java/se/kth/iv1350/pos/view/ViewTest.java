package se.kth.iv1350.pos.view;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.controller.Controller;
import se.kth.iv1350.pos.integration.RegistryCreator;

/**
 * Tests the View class.
 */
public class ViewTest {
    private View view;
    private Controller controller;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        // Redirect System.out to capture output
        System.setOut(new PrintStream(outContent));
        controller = new Controller(RegistryCreator.getInstance());
        view = new View(controller);
    }

    @After
    public void tearDown() {
        // Restore normal System.out
        System.setOut(originalOut);
        view = null;
        controller = null;
    }

    /**
     * Tests basic usage of the fake execution with no exceptions.
     */
    @Test
    public void testRunFakeExecution() {
        // Reset output buffer
        outContent.reset();
        // Run the fake execution
        view.runFakeExecution();
        // Check if output contains expected strings
        String output = outContent.toString();
        assertTrue("Output should contain starting sale message",
                  output.contains("Starting New Sale"));
        assertTrue("Output should contain payment information",
                  output.contains("Customer pays"));
        assertTrue("Output should contain receipt output",
                  output.contains("Begin receipt"));
    }

    /**
     * Tests that scanning an invalid item is handled properly without crashing.
     */
    @Test
    public void testScanInvalidItemException() {
        // Reset output capture
        outContent.reset();

        // Ensure a sale is started
        controller.startNewSale();

        // Try to scan a non-existent item
        boolean result = view.scanItem("non-existent-id", 1);

        // Should return false due to error
        assertFalse("Processing invalid item should return false", result);

        // Verify appropriate error message was displayed
        String output = outContent.toString();
        assertTrue("Error message should be displayed for invalid item",
                  output.contains("ERROR") && output.contains("not found"));
    }

    /**
     * Tests that database connection failure is handled properly without crashing.
     */
    @Test
    public void testDatabaseConnectionErrorException() {
        // Reset output capture
        outContent.reset();

        // Ensure a sale is started
        controller.startNewSale();

        // Try to scan item with ID that causes database connection failure
        boolean result = view.scanItem("999", 1);

        // Should return false due to error
        assertFalse("Processing item with database error should return false", result);

        // Verify appropriate error message was displayed
        String output = outContent.toString();
        assertTrue("Error message should be displayed for database connection failure",
                  output.contains("ERROR") && output.contains("database"));
    }

    /**
     * Tests that the view can continue normal operation after an exception.
     */
    @Test
    public void testRecoveryAfterException() {
        // Ensure a sale is started
        controller.startNewSale();

        // First cause an exception
        view.scanItem("non-existent-id", 1);

        // Clear previous output
        outContent.reset();

        // Then try normal operation
        boolean result = view.scanItem("1", 1); // This should succeed

        // Should return true for successful operation
        assertTrue("Normal operation should succeed after exception", result);

        // Verify normal operation succeeded
        String output = outContent.toString();
        assertTrue("Normal operation should succeed after exception",
                  output.contains("Item ID") && output.contains("1"));
    }

    /**
     * Tests that both errors in runFakeExecution are handled properly.
     */
    @Test
    public void testErrorHandlingInRunFakeExecution() {
        // Reset output buffer
        outContent.reset();
        // Run the fake execution which includes error handling tests
        view.runFakeExecution();
        // Check if output contains error handling messages
        String output = outContent.toString();
        assertTrue("Output should contain invalid item error handling test",
                output.contains("Testing error handling with invalid item"));
        assertTrue("Output should contain database error handling test",
                output.contains("Testing error handling with database connection failure"));
    }
}
