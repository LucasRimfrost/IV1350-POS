package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.util.Amount;

/**
 * Tests the CashRegister class, which represents a cash register in the POS system.
 */
public class CashRegisterTest {
    private CashRegister cashRegister;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        cashRegister = new CashRegister();
    }

    /**
     * Cleans up the test environment after each test.
     */
    @After
    public void tearDown() {
        cashRegister = null;
    }

    /**
     * Tests the default constructor.
     */
    @Test
    public void testDefaultConstructor() {
        assertEquals("Initial balance should be zero",
                    new Amount(0), cashRegister.getBalance());
    }

    /**
     * Tests the constructor with initial balance.
     */
    @Test
    public void testConstructorWithInitialBalance() {
        Amount initialBalance = new Amount(500.0);
        CashRegister registerWithBalance = new CashRegister(initialBalance);

        assertEquals("Initial balance should match the one provided",
                    initialBalance, registerWithBalance.getBalance());
    }

    /**
     * Tests adding a payment to the cash register.
     */
    @Test
    public void testAddPayment() {
        Amount paymentAmount = new Amount(100.0);
        CashPayment payment = new CashPayment(paymentAmount);

        cashRegister.addPayment(payment);

        assertEquals("Balance should be updated after payment",
                    paymentAmount, cashRegister.getBalance());
    }

    /**
     * Tests adding multiple payments to the cash register.
     */
    @Test
    public void testAddMultiplePayments() {
        Amount payment1Amount = new Amount(100.0);
        Amount payment2Amount = new Amount(50.0);
        CashPayment payment1 = new CashPayment(payment1Amount);
        CashPayment payment2 = new CashPayment(payment2Amount);

        cashRegister.addPayment(payment1);
        cashRegister.addPayment(payment2);

        Amount expectedBalance = new Amount(150.0); // 100.0 + 50.0
        assertEquals("Balance should be sum of all payments",
                    expectedBalance, cashRegister.getBalance());
    }

    /**
     * Tests adding a zero payment.
     */
    @Test
    public void testAddZeroPayment() {
        Amount zeroAmount = new Amount(0.0);
        CashPayment payment = new CashPayment(zeroAmount);

        cashRegister.addPayment(payment);

        assertEquals("Balance should remain zero after zero payment",
                    zeroAmount, cashRegister.getBalance());
    }

    /**
     * Tests adding a payment to a register with existing balance.
     */
    @Test
    public void testAddPaymentWithExistingBalance() {
        Amount initialBalance = new Amount(500.0);
        CashRegister registerWithBalance = new CashRegister(initialBalance);

        Amount paymentAmount = new Amount(100.0);
        CashPayment payment = new CashPayment(paymentAmount);

        registerWithBalance.addPayment(payment);

        Amount expectedBalance = new Amount(600.0); // 500.0 + 100.0
        assertEquals("Balance should be initial balance plus payment",
                    expectedBalance, registerWithBalance.getBalance());
    }
}
