package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.CustomerDTO;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.util.Amount;

/**
 * Test class for the Sale class.
 */
public class SaleTest {
    private Sale sale;
    private ItemDTO testItem1;
    private ItemDTO testItem2;
    
    @Before
    public void setUp() {
        sale = new Sale();
        testItem1 = new ItemDTO("1", "TestItem1", "Test item 1 description", new Amount(50.0), 0.25);
        testItem2 = new ItemDTO("2", "TestItem2", "Test item 2 description", new Amount(30.0), 0.12);
    }
    
    @Test
    public void testAddItem() {
        boolean result = sale.addItem(testItem1, 1);
        
        assertTrue("Item should be successfully added", result);
        assertEquals("Sale should contain one item", 1, sale.getItems().size());
        assertEquals("Item in sale should match the added item", testItem1, sale.getItems().get(0).getItem());
        assertEquals("Item quantity should be correct", 1, sale.getItems().get(0).getQuantity());
    }
    
    @Test
    public void testAddSameItemTwice() {
        sale.addItem(testItem1, 1);
        boolean result = sale.addItem(testItem1, 2);
        
        assertTrue("Item should be successfully added", result);
        assertEquals("There should still be only one item", 1, sale.getItems().size());
        assertEquals("Quantity should be incremented", 3, sale.getItems().get(0).getQuantity());
    }
    
    @Test
    public void testAddNullItem() {
        boolean result = sale.addItem(null, 1);
        
        assertFalse("Should return false for null item", result);
        assertEquals("No items should be added", 0, sale.getItems().size());
    }
    
    @Test
    public void testCalculateTotal() {
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0
        
        // Expected: 100.0 + 90.0 = 190.0
        Amount expected = new Amount(190.0);
        Amount actual = sale.calculateTotal();
        
        assertEquals("Total calculated incorrectly", expected, actual);
    }
    
    @Test
    public void testCalculateTotalVat() {
        sale.addItem(testItem1, 2); // 50.0 * 0.25 * 2 = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 0.12 * 3 = 10.8
        
        // Expected: 25.0 + 10.8 = 35.8
        Amount expected = new Amount(35.8);
        Amount actual = sale.calculateTotalVat();
        
        assertEquals("Total VAT calculated incorrectly", expected, actual);
    }
    
    @Test
    public void testCalculateTotalWithVat() {
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0, VAT = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0, VAT = 10.8
        
        // Expected: 100.0 + 90.0 + 25.0 + 10.8 = 225.8
        Amount expected = new Amount(225.8);
        Amount actual = sale.calculateTotalWithVat();
        
        assertEquals("Total with VAT calculated incorrectly", expected, actual);
    }
    
    @Test
    public void testApplyDiscount() {
        sale.addItem(testItem1, 2); // Total with VAT: 125.0
        
        CustomerDTO customer = new CustomerDTO("1001");
        Amount discountAmount = new Amount(10.0);
        
        Amount totalAfterDiscount = sale.applyDiscount(customer, discountAmount);
        Amount expected = new Amount(125.0 - 10.0);
        
        assertEquals("Discount not applied correctly", expected, totalAfterDiscount);
        assertTrue("Sale should indicate a discount was applied", sale.hasDiscount());
        assertEquals("Discount amount not stored correctly", discountAmount, sale.getDiscountAmount());
    }
    
    @Test
    public void testPayWithCashPayment() {
        sale.addItem(testItem1, 1); // 50.0 + 12.5 VAT = 62.5
        Amount paymentAmount = new Amount(100.0);
        CashPayment payment = new CashPayment(paymentAmount);
        
        Amount change = sale.pay(payment);
        Amount expectedChange = new Amount(37.5); // 100.0 - 62.5
        
        assertEquals("Change calculated incorrectly", expectedChange, change);
    }
    
    @Test
    public void testPrintReceipt() {
        // Use a mock printer to verify receipt printing
        sale.addItem(testItem1, 1);
        
        class MockPrinter extends Printer {
            private boolean receiptPrinted = false;
            
            @Override
            public void printReceipt(Receipt receipt) {
                receiptPrinted = true;
                assertNotNull("Receipt should not be null", receipt);
            }
            
            public boolean wasReceiptPrinted() {
                return receiptPrinted;
            }
        }
        
        MockPrinter mockPrinter = new MockPrinter();
        CashPayment payment = new CashPayment(new Amount(100.0));
        sale.pay(payment);
        sale.printReceipt(mockPrinter);
        
        assertTrue("Receipt should have been printed", mockPrinter.wasReceiptPrinted());
    }
}