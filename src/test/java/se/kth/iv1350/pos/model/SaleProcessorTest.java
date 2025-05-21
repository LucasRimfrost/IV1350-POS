package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import se.kth.iv1350.pos.util.Amount;
import java.util.List;

/**
 * Tests the SaleProcessor class, which handles operations spanning multiple domain objects.
 */
public class SaleProcessorTest {
    private SaleProcessor saleProcessor;
    private Sale sale;
    private Receipt receipt;
    private ItemDTO testItem;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        saleProcessor = new SaleProcessor();
        sale = new Sale();
        testItem = new ItemDTO("1", "TestItem", "Test item description", new Amount(50.0), 0.25);
        sale.addItem(testItem, 2);

        // Create a receipt for the sale
        Amount paymentAmount = new Amount(150.0);
        Amount changeAmount = new Amount(25.0);
        receipt = sale.createReceipt(paymentAmount, changeAmount);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        saleProcessor = null;
        sale = null;
        receipt = null;
        testItem = null;
    }

    /**
     * Tests creating a SaleDTO from a Sale.
     */
    @Test
    public void testCreateSaleDTO() {
        SaleDTO saleDTO = saleProcessor.createSaleDTO(sale);

        assertNotNull("SaleDTO should not be null", saleDTO);

        // Verify DTO contains correct data
        assertEquals("Total should match", sale.calculateTotal(), saleDTO.total());
        assertEquals("Total VAT should match", sale.calculateTotalVat(), saleDTO.totalVat());
        assertEquals("Total with VAT should match", sale.calculateTotalWithVat(), saleDTO.totalWithVat());

        // Verify items list
        List<SaleItemDTO> itemDTOs = saleDTO.items();
        assertEquals("Items list should have the correct size", 1, itemDTOs.size());

        // Verify first item
        SaleItemDTO itemDTO = itemDTOs.get(0);
        assertEquals("Item ID should match", testItem.itemID(), itemDTO.item().itemID());
        assertEquals("Item name should match", testItem.name(), itemDTO.item().name());
        assertEquals("Item quantity should match", 2, itemDTO.quantity());
    }
}
