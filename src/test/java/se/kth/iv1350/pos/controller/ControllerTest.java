package se.kth.iv1350.pos.controller;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemRegistrationDTO;
import se.kth.iv1350.pos.dto.PaymentDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
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
        RegistryCreator creator = new RegistryCreator();
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
        assertTrue("Sale should be active after starting", controller.isSaleActive());
    }

    /**
     * Tests entering a valid item.
     */
    @Test
    public void testEnterValidItem() {
        controller.startNewSale();
        ItemRegistrationDTO result = controller.enterItem("1", 1);

        assertNotNull("Result should not be null for valid item", result);
        assertEquals("1", result.item().itemID());
        assertFalse("First entry should not be a duplicate", result.isDuplicate());
    }

    /**
     * Tests entering an invalid item.
     */
    @Test
    public void testEnterInvalidItem() {
        controller.startNewSale();
        ItemRegistrationDTO result = controller.enterItem("999", 1);

        assertNull("Result should be null for invalid item", result);
    }

    /**
     * Tests entering the same item twice to verify duplicate detection.
     */
    @Test
    public void testEnterDuplicateItem() {
        controller.startNewSale();
        controller.enterItem("1", 1);
        ItemRegistrationDTO result = controller.enterItem("1", 1);

        assertTrue("Second entry of same item should be marked as duplicate", result.isDuplicate());
    }

    /**
     * Tests that running total is calculated correctly.
     */
    @Test
    public void testRunningTotal() {
        controller.startNewSale();
        controller.enterItem("1", 1); // Price 10.0 + VAT 1.2 = 11.2

        ItemRegistrationDTO result = controller.enterItem("2", 1); // Price 15.0 + VAT 1.8 = 16.8

        // Total should be 11.2 + 16.8 = 28.0
        BigDecimal expectedTotal = new BigDecimal("28.0").setScale(2);
        BigDecimal actualTotal = result.runningTotal().getValue();

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

        SaleDTO saleInfo = controller.endSale();

        assertNotNull("Sale info should not be null", saleInfo);
        // Total should be 11.2 + 16.8 = 28.0
        BigDecimal expectedTotal = new BigDecimal("28.0").setScale(2);

        assertEquals("End sale should return correct total",
                     0, expectedTotal.compareTo(saleInfo.totalWithVat().getValue()));
    }

    /**
     * Tests processing a payment and calculating change.
     */
    @Test
    public void testProcessPayment() {
        controller.startNewSale();
        controller.enterItem("1", 1); // Price 10.0 + VAT 1.2 = 11.2
        SaleDTO saleInfo = controller.endSale();

        Amount payment = new Amount(20.0);
        PaymentDTO paymentResult = controller.processPayment(payment);

        assertNotNull("Payment result should not be null", paymentResult);

        // Expected change: 20.0 - 11.2 = 8.8
        BigDecimal expectedChange = payment.getValue().subtract(saleInfo.totalWithVat().getValue());
        assertEquals("Change should be calculated correctly",
                0, expectedChange.compareTo(paymentResult.changeAmount().getValue()));
    }

    /**
     * Tests that controller properly tracks running VAT.
     */
    @Test
    public void testRunningVat() {
        controller.startNewSale();
        controller.enterItem("1", 1); // VAT 1.2
        ItemRegistrationDTO result = controller.enterItem("2", 1); // VAT 1.8

        // Total VAT should be 1.2 + 1.8 = 3.0
        BigDecimal expectedVAT = new BigDecimal("3.0").setScale(2);
        assertEquals("Total VAT should be calculated correctly",
                     0, expectedVAT.compareTo(result.runningVat().getValue()));
    }

    /**
     * Tests entering item when no sale has been started.
     */
    @Test
    public void testEnterItemWithoutStartingSale() {
        ItemRegistrationDTO result = controller.enterItem("1", 1);
        assertNull("Result should be null when no sale has been started", result);
    }

    /**
     * Tests ending a sale when no sale has been started.
     */
    @Test
    public void testEndSaleWithoutStartingSale() {
        SaleDTO result = controller.endSale();
        assertNull("Result should be null when no sale has been started", result);
    }

    /**
     * Tests processing payment when no sale has been started.
     */
    @Test
    public void testProcessPaymentWithoutStartingSale() {
        PaymentDTO result = controller.processPayment(new Amount(100.0));
        assertNull("Result should be null when no sale has been started", result);
    }
}
