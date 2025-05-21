package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;
import java.util.List;

/**
 * Tests the Sale class, which represents a single sale transaction.
 */
public class SaleTest {
    private Sale sale;
    private ItemDTO testItem1;
    private ItemDTO testItem2;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        sale = new Sale();
        testItem1 = new ItemDTO("1", "TestItem1", "Test item 1 description", new Amount(50.0), 0.25);
        testItem2 = new ItemDTO("2", "TestItem2", "Test item 2 description", new Amount(30.0), 0.12);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        sale = null;
        testItem1 = null;
        testItem2 = null;
    }

    /**
     * Tests adding an item to the sale.
     */
    @Test
    public void testAddItem() {
        sale.addItem(testItem1, 1);

        List<SaleLineItem> items = sale.getItems();
        assertEquals("Sale should contain one item", 1, items.size());
        assertEquals("Item ID should match", testItem1.itemID(), items.get(0).getItem().itemID());
    }

    /**
     * Tests adding the same item twice to ensure quantity is updated.
     */
    @Test
    public void testAddSameItemTwice() {
        sale.addItem(testItem1, 1);
        sale.addItem(testItem1, 2);

        List<SaleLineItem> items = sale.getItems();
        assertEquals("There should still be only one item", 1, items.size());
        assertEquals("Quantity should be incremented", 3, items.get(0).getQuantity());
    }

    /**
     * Tests adding different items to the sale.
     */
    @Test
    public void testAddDifferentItems() {
        sale.addItem(testItem1, 1);
        sale.addItem(testItem2, 2);

        List<SaleLineItem> items = sale.getItems();
        assertEquals("Sale should contain two different items", 2, items.size());
    }

    /**
     * Tests calculating the total price excluding VAT.
     */
    @Test
    public void testCalculateTotal() {
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0

        Amount total = sale.calculateTotal();
        Amount expected = new Amount(190.0); // 100.0 + 90.0

        assertEquals("Total calculated incorrectly", expected, total);
    }

    /**
     * Tests calculating the total VAT amount.
     */
    @Test
    public void testCalculateTotalVat() {
        sale.addItem(testItem1, 2); // 50.0 * 0.25 * 2 = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 0.12 * 3 = 10.8

        Amount vatAmount = sale.calculateTotalVat();
        Amount expected = new Amount(35.8); // 25.0 + 10.8

        assertEquals("Total VAT calculated incorrectly", expected, vatAmount);
    }

    /**
     * Tests calculating the total price including VAT.
     */
    @Test
    public void testCalculateTotalWithVat() {
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0, VAT = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0, VAT = 10.8

        Amount total = sale.calculateTotalWithVat();
        Amount expected = new Amount(225.8); // 100.0 + 90.0 + 25.0 + 10.8

        assertEquals("Total with VAT calculated incorrectly", expected, total);
    }

    /**
     * Tests making a payment and calculating change.
     */
    @Test
    public void testCreateReceipt() {
        sale.addItem(testItem1, 1); // 50.0 + 12.5 VAT = 62.5
        Amount paymentAmount = new Amount(100.0);
        Amount changeAmount = new Amount(37.5);

        Receipt receipt = sale.createReceipt(paymentAmount, changeAmount);

        assertNotNull("Receipt should not be null", receipt);
        assertEquals("Receipt should have correct payment amount", paymentAmount, receipt.getPaymentAmount());
        assertEquals("Receipt should have correct change amount", changeAmount, receipt.getChangeAmount());
    }

    /**
     * Tests empty sale has zero totals.
     */
    @Test
    public void testEmptySale() {
        // No items added

        assertEquals("Total for empty sale should be zero",
                    new Amount(0), sale.calculateTotal());
        assertEquals("VAT for empty sale should be zero",
                    new Amount(0), sale.calculateTotalVat());
        assertEquals("Total with VAT for empty sale should be zero",
                    new Amount(0), sale.calculateTotalWithVat());
    }

    /**
     * Tests that getItems returns a copy of the list, not the original.
     */
    @Test
    public void testGetItemsReturnsCopy() {
        sale.addItem(testItem1, 1);
        List<SaleLineItem> items = sale.getItems();
        int originalSize = items.size();

        // Attempt to modify the returned list
        try {
            items.clear();
        } catch (UnsupportedOperationException e) {
            // This is good - the list should be unmodifiable
        }

        // Get the items again and verify the original is intact
        List<SaleLineItem> itemsAgain = sale.getItems();
        assertEquals("Original items list should not be affected by modification attempt",
                    originalSize, itemsAgain.size());
    }
}
