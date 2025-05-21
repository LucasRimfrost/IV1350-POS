package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the SaleLineItem class, which represents an item in a sale.
 */
public class SaleLineItemTest {
    private SaleLineItem saleLineItem;
    private ItemDTO testItem;
    private final int INITIAL_QUANTITY = 2;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        testItem = new ItemDTO("1", "TestItem", "Test item description", new Amount(50.0), 0.25);
        saleLineItem = new SaleLineItem(testItem, INITIAL_QUANTITY);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        saleLineItem = null;
        testItem = null;
    }

    /**
     * Tests the constructor and basic getters.
     */
    @Test
    public void testConstructorAndGetters() {
        assertEquals("Item should match the one provided", testItem, saleLineItem.getItem());
        assertEquals("Quantity should match the one provided", INITIAL_QUANTITY, saleLineItem.getQuantity());
    }

    /**
     * Tests incrementing the quantity.
     */
    @Test
    public void testIncrementQuantity() {
        int additionalQuantity = 3;
        saleLineItem.incrementQuantity(additionalQuantity);

        assertEquals("Quantity should be incremented correctly",
                    INITIAL_QUANTITY + additionalQuantity, saleLineItem.getQuantity());
    }

    /**
     * Tests calculating the subtotal (price excluding VAT).
     */
    @Test
    public void testGetSubtotal() {
        // Expected: 50.0 * 2 = 100.0
        Amount expected = new Amount(100.0);
        Amount actual = saleLineItem.getSubtotal();

        assertEquals("Subtotal calculated incorrectly", expected, actual);
    }

    /**
     * Tests calculating the VAT amount.
     */
    @Test
    public void testGetVatAmount() {
        // Expected: 50.0 * 0.25 * 2 = 25.0
        Amount expected = new Amount(25.0);
        Amount actual = saleLineItem.getVatAmount();

        assertEquals("VAT amount calculated incorrectly", expected, actual);
    }

    /**
     * Tests calculating the total with VAT.
     */
    @Test
    public void testGetTotalWithVat() {
        // Expected: (50.0 + 50.0 * 0.25) * 2 = 125.0
        Amount expected = new Amount(125.0);
        Amount actual = saleLineItem.getTotalWithVat();

        assertEquals("Total with VAT calculated incorrectly", expected, actual);
    }

    /**
     * Tests that calculations are correct with zero quantity.
     */
    @Test
    public void testZeroQuantity() {
        SaleLineItem zeroItem = new SaleLineItem(testItem, 0);

        assertEquals("Subtotal should be zero with zero quantity",
                    new Amount(0), zeroItem.getSubtotal());
        assertEquals("VAT amount should be zero with zero quantity",
                    new Amount(0), zeroItem.getVatAmount());
        assertEquals("Total with VAT should be zero with zero quantity",
                    new Amount(0), zeroItem.getTotalWithVat());
    }
}
