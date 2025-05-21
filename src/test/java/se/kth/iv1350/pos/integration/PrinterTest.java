package se.kth.iv1350.pos.integration;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.ReceiptDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the Printer class, which is responsible for printing receipts.
 */
@RunWith(JUnit4.class)
public class PrinterTest {
    private Printer printer;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private ReceiptDTO receiptDTO;

    /**
     * Sets up the test environment before each test.
     * Redirects System.out to capture output.
     */
    @Before
    public void setUp() {
        printer = new Printer();

        // Set up capture of System.out
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Create a test receipt
        ItemDTO itemDTO = new ItemDTO("1", "Test Item", "Test description", new Amount(50.0), 0.25);
        SaleItemDTO saleItemDTO = new SaleItemDTO(
            itemDTO, 2, new Amount(100.0), new Amount(25.0), new Amount(125.0)
        );
        List<SaleItemDTO> items = new ArrayList<>();
        items.add(saleItemDTO);

        receiptDTO = new ReceiptDTO(
            items,
            new Amount(100.0),
            new Amount(25.0),
            new Amount(150.0),
            new Amount(25.0),
            LocalDateTime.now()
        );
    }

    /**
     * Cleans up the test environment after each test.
     * Restores original System.out.
     */
    @After
    public void tearDown() {
        printer = null;
        receiptDTO = null;
        System.setOut(originalOut);
    }

    /**
     * Tests that printing a receipt produces output.
     */
    @Test
    public void testPrintReceipt() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertNotEquals("Printing should produce output", 0, output.length());
    }

    /**
     * Tests that the receipt output contains expected header text.
     */
    @Test
    public void testReceiptContainsHeader() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertTrue("Receipt should contain header",
                  output.contains("Begin receipt"));
        assertTrue("Receipt should contain time of sale",
                  output.contains("Time of Sale"));
    }

    /**
     * Tests that the receipt output contains item information.
     */
    @Test
    public void testReceiptContainsItemInfo() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertTrue("Receipt should contain item name",
                  output.contains("Test Item"));
        assertTrue("Receipt should show quantity",
                  output.contains("2 x"));
    }

    /**
     * Tests that the receipt output contains total information.
     */
    @Test
    public void testReceiptContainsTotals() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertTrue("Receipt should show total",
                  output.contains("Total :"));
        assertTrue("Receipt should show VAT",
                  output.contains("VAT :"));
    }

    /**
     * Tests that the receipt output contains payment information.
     */
    @Test
    public void testReceiptContainsPaymentInfo() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertTrue("Receipt should show cash amount",
                  output.contains("Cash :"));
        assertTrue("Receipt should show change amount",
                  output.contains("Change :"));
    }

    /**
     * Tests that the receipt output contains footer.
     */
    @Test
    public void testReceiptContainsFooter() {
        printer.printReceipt(receiptDTO);

        String output = outContent.toString();
        assertTrue("Receipt should contain footer",
                  output.contains("End receipt"));
    }

    /**
     * Tests printing a receipt with no items.
     */
    @Test
    public void testPrintEmptyReceipt() {
        ReceiptDTO emptyReceipt = new ReceiptDTO(
            new ArrayList<>(),
            new Amount(0),
            new Amount(0),
            new Amount(0),
            new Amount(0),
            LocalDateTime.now()
        );

        printer.printReceipt(emptyReceipt);

        String output = outContent.toString();
        assertNotEquals("Printing empty receipt should still produce output",
                       0, output.length());
        assertTrue("Empty receipt should contain header and footer",
                  output.contains("Begin receipt") && output.contains("End receipt"));
    }
}
