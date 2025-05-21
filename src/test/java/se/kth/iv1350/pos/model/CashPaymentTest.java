package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the CashPayment class, which represents a cash payment in a sale.
 */
public class CashPaymentTest {
    private CashPayment payment;
    private final Amount PAID_AMOUNT = new Amount(100.0);

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        payment = new CashPayment(PAID_AMOUNT);
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        payment = null;
    }

    /**
     * Tests that the constructor sets the paid amount correctly.
     */
    @Test
    public void testConstructor() {
        assertEquals("Paid amount should match the one provided",
                    PAID_AMOUNT, payment.getAmount());
    }

    /**
     * Tests calculating change when total is less than paid amount.
     */
    @Test
    public void testGetChangeWithPositiveChange() {
        Amount totalAmount = new Amount(75.0);
        Amount expectedChange = new Amount(25.0); // 100.0 - 75.0
        Amount actualChange = payment.getChange(totalAmount);

        assertEquals("Change calculated incorrectly for positive change",
                    expectedChange, actualChange);
    }

    /**
     * Tests calculating change when total equals paid amount.
     */
    @Test
    public void testGetChangeWithZeroChange() {
        Amount totalAmount = new Amount(100.0);
        Amount expectedChange = new Amount(0.0); // 100.0 - 100.0
        Amount actualChange = payment.getChange(totalAmount);

        assertEquals("Change calculated incorrectly for zero change",
                    expectedChange, actualChange);
    }

    /**
     * Tests calculating change when total is more than paid amount.
     * This would normally be an error, but the class should still calculate correctly.
     */
    @Test
    public void testGetChangeWithNegativeChange() {
        Amount totalAmount = new Amount(125.0);
        Amount expectedChange = new Amount(-25.0); // 100.0 - 125.0
        Amount actualChange = payment.getChange(totalAmount);

        assertEquals("Change calculated incorrectly for negative change",
                    expectedChange, actualChange);
    }

    /**
     * Tests calculating change with zero paid amount.
     */
    @Test
    public void testGetChangeWithZeroPaidAmount() {
        CashPayment zeroPayment = new CashPayment(new Amount(0.0));
        Amount totalAmount = new Amount(50.0);
        Amount expectedChange = new Amount(-50.0); // 0.0 - 50.0
        Amount actualChange = zeroPayment.getChange(totalAmount);

        assertEquals("Change calculated incorrectly for zero paid amount",
                    expectedChange, actualChange);
    }
}
