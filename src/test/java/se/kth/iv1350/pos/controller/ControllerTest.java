package se.kth.iv1350.pos.controller;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.*;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.util.Amount;
import java.util.List;

/**
 * Tests the Controller class, which coordinates all operations in the
 * Point-of-Sale system including starting sales, entering items,
 * applying discounts, and handling payments.
 */
public class ControllerTest {
    private Controller controller;
    private MockItemRegistry mockItemRegistry;
    private MockDiscountRegistry mockDiscountRegistry;
    private MockPrinter mockPrinter;
    private MockAccountingSystem mockAccountingSystem;

    /**
     * Mock class for the ItemRegistry used to simulate inventory operations.
     */
    private class MockItemRegistry extends ItemRegistry {
        private boolean inventoryUpdated = false;

        @Override
        public ItemDTO findItem(String itemID) {
            if ("1".equals(itemID)) {
                return new ItemDTO("1", "TestItem1", "Test item 1 description", new Amount(50.0), 0.25);
            } else if ("2".equals(itemID)) {
                return new ItemDTO("2", "TestItem2", "Test item 2 description", new Amount(30.0), 0.12);
            }
            return null;
        }

        @Override
        public boolean updateInventory(String itemID, int quantity) {
            inventoryUpdated = true;
            return true;
        }

        /**
         * Checks if the inventory was updated.
         *
         * @return true if the inventory was updated, false otherwise.
         */
        public boolean wasInventoryUpdated() {
            return inventoryUpdated;
        }
    }

    /**
     * Mock class for the DiscountRegistry used to simulate discount operations.
     */
    private class MockDiscountRegistry extends DiscountRegistry {
        private boolean discountRequested = false;
        private String lastCustomerId = null;

        @Override
        public Amount getDiscount(List saleItems, Amount totalAmount, String customerID) {
            discountRequested = true;
            lastCustomerId = customerID;
            if ("1001".equals(customerID)) {
                return new Amount(10.0);
            }
            return new Amount(0.0);
        }

        /**
         * Checks if a discount was requested.
         *
         * @return true if a discount was requested, false otherwise.
         */
        public boolean wasDiscountRequested() {
            return discountRequested;
        }

        /**
         * Gets the ID of the customer who last requested a discount.
         *
         * @return the last customer ID, or null if no discount was requested.
         */
        public String getLastCustomerId() {
            return lastCustomerId;
        }
    }

    /**
     * Mock class for the Printer used to verify receipt printing.
     */
    private class MockPrinter extends Printer {
        private boolean receiptPrinted = false;

        @Override
        public void printReceipt(se.kth.iv1350.pos.model.Receipt receipt) {
            receiptPrinted = true;
        }

        /**
         * Checks if a receipt was printed.
         *
         * @return true if a receipt was printed, false otherwise.
         */
        public boolean wasReceiptPrinted() {
            return receiptPrinted;
        }
    }

    /**
     * Mock class for the AccountingSystem used to verify sale recording.
     */
    private class MockAccountingSystem extends AccountingSystem {
        private boolean saleRecorded = false;
        private Amount lastStatisticsAmount = null;

        @Override
        public void recordSale(Sale sale) {
            saleRecorded = true;
        }

        @Override
        public void updateSalesStatistics(Amount saleAmount) {
            lastStatisticsAmount = saleAmount;
        }

        /**
         * Checks if a sale was recorded.
         *
         * @return true if a sale was recorded, false otherwise.
         */
        public boolean wasSaleRecorded() {
            return saleRecorded;
        }

        /**
         * Gets the last amount used to update sales statistics.
         *
         * @return the last statistics amount, or null if not updated.
         */
        public Amount getLastStatisticsAmount() {
            return lastStatisticsAmount;
        }
    }

    /**
     * Mock class for the RegistryCreator used to inject mock dependencies.
     */
    private class MockRegistryCreator extends RegistryCreator {
        private final MockItemRegistry mockItemRegistry;
        private final MockDiscountRegistry mockDiscountRegistry;
        private final MockAccountingSystem mockAccountingSystem;
        private final MockPrinter mockPrinter;

        /**
         * Creates a new instance with the specified mock components.
         */
        public MockRegistryCreator(MockItemRegistry mockItemRegistry,
                                  MockDiscountRegistry mockDiscountRegistry,
                                  MockAccountingSystem mockAccountingSystem,
                                  MockPrinter mockPrinter) {
            this.mockItemRegistry = mockItemRegistry;
            this.mockDiscountRegistry = mockDiscountRegistry;
            this.mockAccountingSystem = mockAccountingSystem;
            this.mockPrinter = mockPrinter;
        }

        @Override
        public ItemRegistry getItemRegistry() {
            return mockItemRegistry;
        }

        @Override
        public DiscountRegistry getDiscountRegistry() {
            return mockDiscountRegistry;
        }

        @Override
        public AccountingSystem getAccountingSystem() {
            return mockAccountingSystem;
        }

        @Override
        public Printer getPrinter() {
            return mockPrinter;
        }
    }

    /**
     * Mock class for ExternalSystemObserver used to verify observer notifications.
     */
    private class MockExternalSystemObserver implements Controller.ExternalSystemObserver {
        private boolean notified = false;
        private Sale lastCompletedSale = null;

        @Override
        public void saleCompleted(Sale completedSale) {
            notified = true;
            lastCompletedSale = completedSale;
        }

        /**
         * Checks if the observer was notified.
         *
         * @return true if the observer was notified, false otherwise.
         */
        public boolean wasNotified() {
            return notified;
        }

        /**
         * Gets the last completed sale passed to the observer.
         *
         * @return the last completed sale, or null if not notified.
         */
        public Sale getLastCompletedSale() {
            return lastCompletedSale;
        }
    }

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        mockItemRegistry = new MockItemRegistry();
        mockDiscountRegistry = new MockDiscountRegistry();
        mockPrinter = new MockPrinter();
        mockAccountingSystem = new MockAccountingSystem();

        MockRegistryCreator mockCreator = new MockRegistryCreator(
            mockItemRegistry, mockDiscountRegistry, mockAccountingSystem, mockPrinter
        );

        controller = new Controller(mockCreator);
    }

    /**
     * Tests if a new sale can be started successfully.
     */
    @Test
    public void testStartNewSale() {
        controller.startNewSale();
        assertNotNull("Sale should be created", controller.getCurrentSale());
    }

    /**
     * Tests entering a valid item into the sale.
     */
    @Test
    public void testEnterValidItem() {
        // Arrange
        controller.startNewSale();

        // Act
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);

        // Assert
        assertNotNull("Result should not be null for valid item", result);
        assertEquals("Item ID should match", "1", result.getItem().getItemID());
        assertFalse("First entry should not be a duplicate", result.isDuplicate());
    }

    /**
     * Tests entering an invalid item ID.
     */
    @Test
    public void testEnterInvalidItem() {
        // Arrange
        controller.startNewSale();

        // Act
        Controller.ItemWithRunningTotal result = controller.enterItem("999", 1);

        // Assert
        assertNull("Result should be null for invalid item", result);
    }

    /**
     * Tests entering the same item twice to verify duplicate detection.
     */
    @Test
    public void testEnterDuplicateItem() {
        // Arrange
        controller.startNewSale();
        controller.enterItem("1", 1);

        // Act
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);

        // Assert
        assertTrue("Second entry of same item should be marked as duplicate", result.isDuplicate());
    }

    /**
     * Tests ending a sale and getting the final total.
     */
    @Test
    public void testEndSale() {
        // Arrange
        controller.startNewSale();
        controller.enterItem("1", 2); // 50.0 * 2 = 100.0, VAT = 25.0
        controller.enterItem("2", 1); // 30.0 * 1 = 30.0, VAT = 3.6

        // Act
        Amount total = controller.endSale();

        // Assert
        Amount expected = new Amount(158.6); // 100.0 + 30.0 + 25.0 + 3.6
        assertEquals("End sale should return correct total", expected, total);
    }

    /**
     * Tests requesting a discount.
     */
    @Test
    public void testRequestDiscount() {
        // Arrange
        controller.startNewSale();
        controller.enterItem("1", 1); // 50.0 + 12.5 VAT = 62.5

        // Act
        Amount totalAfterDiscount = controller.requestDiscount("1001");

        // Assert
        Amount expected = new Amount(52.5); // 62.5 - 10.0
        assertEquals("Discount should be applied correctly", expected, totalAfterDiscount);
        assertTrue("Discount registry should be called", mockDiscountRegistry.wasDiscountRequested());
        assertEquals("Correct customer ID should be used", "1001", mockDiscountRegistry.getLastCustomerId());
    }

    /**
     * Tests processing a payment and calculating change.
     */
    @Test
    public void testPay() {
        // Arrange
        controller.startNewSale();
        controller.enterItem("1", 1); // 50.0 + 12.5 VAT = 62.5

        // Act
        Amount change = controller.processPayment(new Amount(100.0));

        // Assert
        Amount expectedChange = new Amount(37.5); // 100.0 - 62.5
        assertEquals("Change should be calculated correctly", expectedChange, change);
        assertTrue("Inventory should be updated", mockItemRegistry.wasInventoryUpdated());
        assertTrue("Receipt should be printed", mockPrinter.wasReceiptPrinted());
        assertTrue("Sale should be recorded in accounting", mockAccountingSystem.wasSaleRecorded());
        assertNotNull("Sales statistics should be updated", mockAccountingSystem.getLastStatisticsAmount());
    }

    /**
     * Tests notification of external system observers.
     */
    @Test
    public void testExternalSystemObserver() {
        // Arrange
        MockExternalSystemObserver observer = new MockExternalSystemObserver();
        controller.addExternalSystemObserver(observer);
        controller.startNewSale();
        controller.enterItem("1", 1);

        // Act
        controller.processPayment(new Amount(100.0));

        // Assert
        assertTrue("External system observer should be notified", observer.wasNotified());
        assertNotNull("Observer should receive the completed sale", observer.getLastCompletedSale());
    }

    /**
     * Tests that the inner class ItemWithRunningTotal correctly stores information.
     */
    @Test
    public void testItemWithRunningTotalClass() {
        // Arrange
        ItemDTO testItem = new ItemDTO("1", "TestItem", "Description", new Amount(10.0), 0.25);
        Amount testTotal = new Amount(12.5);
        boolean testIsDuplicate = true;

        // Act
        Controller.ItemWithRunningTotal result = new Controller.ItemWithRunningTotal(
            testItem, testTotal, testIsDuplicate);

        // Assert
        assertSame("Item should be stored correctly", testItem, result.getItem());
        assertEquals("Running total should be stored correctly", testTotal, result.getRunningTotal());
        assertTrue("Duplicate flag should be stored correctly", result.isDuplicate());
    }
}
