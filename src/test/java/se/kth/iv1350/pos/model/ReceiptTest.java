package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tests the Receipt class, which represents a receipt for a completed sale.
 */
public class ReceiptTest {
    private Receipt receipt;
    private Sale sale;
    private Amount paymentAmount;
    private Amount changeAmount;
    private ItemDTO testItem;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        // Create a sale with one item
        sale = new Sale();
        testItem = new ItemDTO("1", "TestItem", "Test description", new Amount(50.0), 0.25);
        sale.addItem(testItem, 2);

        // Set up payment details
        paymentAmount = new Amount(150.0);
        changeAmount = new Amount(25.0);

        // Create receipt
        receipt = new Receipt(sale, paymentAmount, changeAmount);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        receipt = null;
        sale = null;
        paymentAmount = null;
        changeAmount = null;
        testItem = null;
    }

    /**
     * Tests the constructor and basic getters.
     */
    @Test
    public void testConstructorAndGetters() {
        assertEquals("Payment amount should match", paymentAmount, receipt.getPaymentAmount());
        assertEquals("Change amount should match", changeAmount, receipt.getChangeAmount());
        assertNotNull("Sale time should not be null", receipt.getSaleTime());

        // Verify the sale data was copied correctly
        List<SaleLineItem> items = receipt.getItems();
        assertEquals("Items list should have correct size", 1, items.size());
        assertEquals("Item should match", testItem.itemID(), items.get(0).getItem().itemID());
        assertEquals("Item quantity should match", 2, items.get(0).getQuantity());
    }

    /**
     * Tests that the receipt correctly copies the total amount from the sale.
     */
    @Test
    public void testTotalAmount() {
        // Expected: 50.0 * 2 = 100.0
        Amount expected = new Amount(100.0);
        Amount actual = receipt.getTotalAmount();

        assertEquals("Total amount should match sale total", expected, actual);
    }

    /**
     * Tests that the receipt correctly copies the VAT amount from the sale.
     */
    @Test
    public void testTotalVat() {
        // Expected: 50.0 * 0.25 * 2 = 25.0
        Amount expected = new Amount(25.0);
        Amount actual = receipt.getTotalVat();

        assertEquals("Total VAT should match sale VAT", expected, actual);
    }

    /**
     * Tests that the receipt creates a new copy of the items list, not a reference.
     */
    @Test
    public void testItemsListIsCopy() {
        List<SaleLineItem> items = receipt.getItems();
        int originalSize = items.size();

        // Attempt to modify the returned list
        try {
            items.clear();
        } catch (UnsupportedOperationException e) {
            // This is expected - the list should be unmodifiable
        }

        // Get the items again and verify the original is intact
        List<SaleLineItem> itemsAgain = receipt.getItems();
        assertEquals("Original items list should not be affected by modification attempt",
                     originalSize, itemsAgain.size());
    }

    /**
     * Tests creating a receipt for an empty sale.
     */
    @Test
    public void testEmptySale() {
        Sale emptySale = new Sale();
        Receipt emptyReceipt = new Receipt(emptySale, paymentAmount, changeAmount);

        assertEquals("Items list should be empty", 0, emptyReceipt.getItems().size());
        assertEquals("Total amount should be zero", new Amount(0), emptyReceipt.getTotalAmount());
        assertEquals("Total VAT should be zero", new Amount(0), emptyReceipt.getTotalVat());
    }

    /**
     * Tests that the receipt captures the current time at creation.
     */
    @Test
    public void testSaleTimeIsCurrentTime() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Receipt newReceipt = new Receipt(sale, paymentAmount, changeAmount);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        LocalDateTime receiptTime = newReceipt.getSaleTime();

        assertTrue("Receipt time should be after test start",
                   receiptTime.isAfter(before) || receiptTime.isEqual(before));
        assertTrue("Receipt time should be before test end",
                   receiptTime.isBefore(after) || receiptTime.isEqual(after));
    }
}
