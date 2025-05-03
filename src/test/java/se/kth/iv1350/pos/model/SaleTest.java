package se.kth.iv1350.pos.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.CustomerDTO;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.util.Amount;
import java.util.List;

/**
 * Tests the Sale class, which represents a single sale transaction and handles
 * items, pricing, discounts, and payment processing.
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
     * Tests adding an item to the sale.
     */
    @Test
    public void testAddItem() {
        // Act
        sale.addItem(testItem1, 1);

        // Assert
        List<SaleLineItem> items = sale.getItems();
        assertEquals("Sale should contain one item", 1, items.size());
        assertEquals("Item in sale should match the added item", testItem1, items.get(0).getItem());
        assertEquals("Item quantity should be correct", 1, items.get(0).getQuantity());
    }

    /**
     * Tests adding the same item twice to ensure quantity is updated.
     */
    @Test
    public void testAddSameItemTwice() {
        // Arrange
        sale.addItem(testItem1, 1);

        // Act
        sale.addItem(testItem1, 2);

        // Assert
        List<SaleLineItem> items = sale.getItems();
        assertEquals("There should still be only one item", 1, items.size());
        assertEquals("Quantity should be incremented", 3, items.get(0).getQuantity());
    }

    /**
     * Tests adding different items to the sale.
     */
    @Test
    public void testAddDifferentItems() {
        // Act
        sale.addItem(testItem1, 1);
        sale.addItem(testItem2, 2);

        // Assert
        List<SaleLineItem> items = sale.getItems();
        assertEquals("Sale should contain two different items", 2, items.size());
        assertEquals("First item should match", testItem1, items.get(0).getItem());
        assertEquals("Second item should match", testItem2, items.get(1).getItem());
        assertEquals("First item quantity should be correct", 1, items.get(0).getQuantity());
        assertEquals("Second item quantity should be correct", 2, items.get(1).getQuantity());
    }

    /**
     * Tests calculating the total price excluding VAT.
     */
    @Test
    public void testCalculateTotal() {
        // Arrange
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0

        // Act
        Amount actual = sale.calculateTotal();

        // Assert
        Amount expected = new Amount(190.0); // 100.0 + 90.0
        assertEquals("Total calculated incorrectly", expected, actual);
    }

    /**
     * Tests calculating the total VAT amount.
     */
    @Test
    public void testCalculateTotalVat() {
        // Arrange
        sale.addItem(testItem1, 2); // 50.0 * 0.25 * 2 = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 0.12 * 3 = 10.8

        // Act
        Amount actual = sale.calculateTotalVat();

        // Assert
        Amount expected = new Amount(35.8); // 25.0 + 10.8
        assertEquals("Total VAT calculated incorrectly", expected, actual);
    }

    /**
     * Tests calculating the total price including VAT.
     */
    @Test
    public void testCalculateTotalWithVat() {
        // Arrange
        sale.addItem(testItem1, 2); // 50.0 * 2 = 100.0, VAT = 25.0
        sale.addItem(testItem2, 3); // 30.0 * 3 = 90.0, VAT = 10.8

        // Act
        Amount actual = sale.calculateTotalWithVat();

        // Assert
        Amount expected = new Amount(225.8); // 100.0 + 90.0 + 25.0 + 10.8
        assertEquals("Total with VAT calculated incorrectly", expected, actual);
    }

    /**
     * Tests applying a discount to the sale.
     */
    @Test
    public void testApplyDiscount() {
        // Arrange
        sale.addItem(testItem1, 2); // Total with VAT: 125.0
        CustomerDTO customer = new CustomerDTO("1001");
        Amount discountAmount = new Amount(10.0);

        // Act
        Amount totalAfterDiscount = sale.applyDiscount(customer, discountAmount);

        // Assert
        Amount expected = new Amount(125.0 - 10.0);
        assertEquals("Discount not applied correctly", expected, totalAfterDiscount);
        assertTrue("Sale should indicate a discount was applied", sale.hasDiscount());
        assertEquals("Discount amount not stored correctly", discountAmount, sale.getDiscountAmount());
    }

    /**
     * Tests that the discount indicator works correctly.
     */
    @Test
    public void testHasDiscount() {
        // Arrange
        assertFalse("New sale should not have a discount", sale.hasDiscount());

        // Act
        sale.applyDiscount(new CustomerDTO("1001"), new Amount(10.0));

        // Assert
        assertTrue("Sale should indicate discount after applying one", sale.hasDiscount());
    }

    /**
     * Tests making a payment and calculating change.
     */
    @Test
    public void testPayWithCashPayment() {
        // Arrange
        sale.addItem(testItem1, 1); // 50.0 + 12.5 VAT = 62.5
        Amount paymentAmount = new Amount(100.0);
        CashPayment payment = new CashPayment(paymentAmount);

        // Act
        Amount change = sale.pay(payment);

        // Assert
        Amount expectedChange = new Amount(37.5); // 100.0 - 62.5
        assertEquals("Change calculated incorrectly", expectedChange, change);
    }

    /**
     * Tests printing a receipt via a mock printer.
     */
    @Test
    public void testPrintReceipt() {
        // Arrange
        class MockPrinter extends Printer {
            private boolean receiptPrinted = false;
            private Receipt lastReceipt = null;

            @Override
            public void printReceipt(Receipt receipt) {
                receiptPrinted = true;
                lastReceipt = receipt;
            }

            public boolean wasReceiptPrinted() {
                return receiptPrinted;
            }

            public Receipt getLastReceipt() {
                return lastReceipt;
            }
        }

        MockPrinter mockPrinter = new MockPrinter();
        sale.addItem(testItem1, 1);
        CashPayment payment = new CashPayment(new Amount(100.0));
        sale.pay(payment);

        // Act
        sale.printReceipt(mockPrinter);

        // Assert
        assertTrue("Receipt should have been printed", mockPrinter.wasReceiptPrinted());
        assertNotNull("Printer should receive a receipt", mockPrinter.getLastReceipt());
    }

    /**
     * Tests that receipt printing doesn't happen if no payment has been made.
     */
    @Test
    public void testPrintReceiptWithoutPayment() {
        // Arrange
        class MockPrinter extends Printer {
            private boolean receiptPrinted = false;

            @Override
            public void printReceipt(Receipt receipt) {
                receiptPrinted = true;
            }

            public boolean wasReceiptPrinted() {
                return receiptPrinted;
            }
        }

        MockPrinter mockPrinter = new MockPrinter();
        sale.addItem(testItem1, 1);

        // Act
        sale.printReceipt(mockPrinter);

        // Assert
        assertFalse("Receipt should not be printed before payment", mockPrinter.wasReceiptPrinted());
    }

    /**
     * Tests updating inventory via the ItemRegistry.
     */
    @Test
    public void testUpdateInventory() {
        // Arrange
        sale.addItem(testItem1, 2);

        class MockItemRegistry extends ItemRegistry {
            private boolean inventoryUpdated = false;
            private List<SaleLineItem> lastItems = null;

            @Override
            public boolean updateInventoryForCompletedSale(List<SaleLineItem> saleItems) {
                inventoryUpdated = true;
                lastItems = saleItems;
                return true;
            }

            public boolean wasInventoryUpdated() {
                return inventoryUpdated;
            }

            public List<SaleLineItem> getLastItems() {
                return lastItems;
            }
        }

        MockItemRegistry mockRegistry = new MockItemRegistry();

        // Act
        boolean result = sale.updateInventory(mockRegistry);

        // Assert
        assertTrue("Inventory update should succeed", result);
        assertTrue("Inventory should be updated", mockRegistry.wasInventoryUpdated());
        assertNotNull("Sale items should be passed to registry", mockRegistry.getLastItems());
        assertEquals("Correct number of items should be passed", 1, mockRegistry.getLastItems().size());
    }
}
