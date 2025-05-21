package se.kth.iv1350.pos.integration;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the AccountingSystem class, which contains calls to the external accounting system.
 */
public class AccountingSystemTest {
    private AccountingSystem accountingSystem;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private SaleDTO saleDTO;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        accountingSystem = new AccountingSystem();

        // Set up capture of System.out
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Create a test sale
        ItemDTO itemDTO = new ItemDTO("1", "Test Item", "Test description", new Amount(50.0), 0.25);
        SaleItemDTO saleItemDTO = new SaleItemDTO(
            itemDTO, 2, new Amount(100.0), new Amount(25.0), new Amount(125.0)
        );
        List<SaleItemDTO> items = new ArrayList<>();
        items.add(saleItemDTO);

        saleDTO = new SaleDTO(
            items,
            new Amount(100.0),
            new Amount(25.0),
            new Amount(0.0),
            new Amount(125.0),
            LocalDateTime.now()
        );
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        accountingSystem = null;
        saleDTO = null;
        System.setOut(originalOut);
    }

    /**
     * Tests recording a sale.
     */
    @Test
    public void testRecordSale() {
        accountingSystem.recordSale(saleDTO);

        String output = outContent.toString();
        assertTrue("Sale recording should be logged",
                  output.contains("Sale recorded in accounting system"));
        assertTrue("Total amount should be logged",
                  output.contains("Total amount:"));
        assertTrue("VAT amount should be logged",
                  output.contains("Total VAT:"));
    }

    /**
     * Tests updating sales statistics.
     */
    @Test
    public void testUpdateSalesStatistics() {
        Amount saleAmount = new Amount(125.0);
        accountingSystem.updateSalesStatistics(saleAmount);

        String output = outContent.toString();
        assertTrue("Statistics update should be logged",
                  output.contains("Sales statistics updated"));
        assertTrue("Sale amount should be logged",
                  output.contains(saleAmount.toString()));
    }

    /**
     * Tests recording a sale with zero amount.
     */
    @Test
    public void testRecordZeroSale() {
        SaleDTO zeroSale = new SaleDTO(
            new ArrayList<>(),
            new Amount(0.0),
            new Amount(0.0),
            new Amount(0.0),
            new Amount(0.0),
            LocalDateTime.now()
        );

        accountingSystem.recordSale(zeroSale);

        String output = outContent.toString();
        assertTrue("Zero sale recording should be logged",
                  output.contains("Sale recorded in accounting system"));
    }

    /**
     * Tests updating sales statistics with zero amount.
     */
    @Test
    public void testUpdateSalesStatisticsWithZeroAmount() {
        Amount zeroAmount = new Amount(0.0);
        accountingSystem.updateSalesStatistics(zeroAmount);

        String output = outContent.toString();
        assertTrue("Zero statistics update should be logged",
                  output.contains("Sales statistics updated"));
    }

    /**
     * Tests that both operations can be called in sequence.
     */
    @Test
    public void testBothOperationsInSequence() {
        // Clear output from previous tests
        outContent.reset();

        // Perform operations
        accountingSystem.recordSale(saleDTO);
        accountingSystem.updateSalesStatistics(saleDTO.totalWithVat());

        // Verify output
        String output = outContent.toString();
        assertTrue("Sale recording should be logged",
                  output.contains("Sale recorded in accounting system"));
        assertTrue("Statistics update should be logged",
                  output.contains("Sales statistics updated"));
    }
}
