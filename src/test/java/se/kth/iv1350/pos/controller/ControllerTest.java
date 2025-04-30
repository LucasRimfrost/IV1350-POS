package se.kth.iv1350.pos.controller;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.integration.AccountingSystem;
import se.kth.iv1350.pos.integration.DiscountRegistry;
import se.kth.iv1350.pos.integration.ItemRegistry;
import se.kth.iv1350.pos.integration.Printer;
import se.kth.iv1350.pos.integration.RegistryCreator;
import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.util.Amount;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class for the Controller class.
 */
public class ControllerTest {
    private Controller controller;
    private MockRegistryCreator mockCreator;
    private MockItemRegistry mockItemRegistry;
    private MockPrinter mockPrinter;
    
    /**
     * Mock class for the ItemRegistry
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
        
        public boolean wasInventoryUpdated() {
            return inventoryUpdated;
        }
    }
    
    /**
     * Mock class for the DiscountRegistry
     */
    private class MockDiscountRegistry extends DiscountRegistry {
        private boolean discountRequested = false;
        
        @Override
        public Amount getDiscount(List saleItems, Amount totalAmount, String customerID) {
            discountRequested = true;
            if ("1001".equals(customerID)) {
                return new Amount(10.0);
            }
            return new Amount(0.0);
        }
        
        public boolean wasDiscountRequested() {
            return discountRequested;
        }
    }
    
    /**
     * Mock class for the Printer
     */
    private class MockPrinter extends Printer {
        private boolean receiptPrinted = false;
        
        @Override
        public void printReceipt(se.kth.iv1350.pos.model.Receipt receipt) {
            receiptPrinted = true;
        }
        
        public boolean wasReceiptPrinted() {
            return receiptPrinted;
        }
    }
    
    /**
     * Mock class for the AccountingSystem
     */
    private class MockAccountingSystem extends AccountingSystem {
        private boolean saleRecorded = false;
        
        @Override
        public void recordSale(Sale sale) {
            saleRecorded = true;
        }
        
        public boolean wasSaleRecorded() {
            return saleRecorded;
        }
    }
    
    /**
     * Mock class for the RegistryCreator
     */
    private class MockRegistryCreator extends RegistryCreator {
        private final MockItemRegistry mockItemRegistry;
        private final MockDiscountRegistry mockDiscountRegistry;
        private final MockAccountingSystem mockAccountingSystem;
        
        public MockRegistryCreator(MockItemRegistry mockItemRegistry, MockDiscountRegistry mockDiscountRegistry) {
            this.mockItemRegistry = mockItemRegistry;
            this.mockDiscountRegistry = mockDiscountRegistry;
            this.mockAccountingSystem = new MockAccountingSystem();
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
     * Mock class for ExternalSystemObserver
     */
    private class MockExternalSystemObserver implements Controller.ExternalSystemObserver {
        private boolean notified = false;
        
        @Override
        public void saleCompleted(Sale completedSale) {
            notified = true;
        }
        
        public boolean wasNotified() {
            return notified;
        }
    }
    
    @Before
    public void setUp() {
        mockItemRegistry = new MockItemRegistry();
        MockDiscountRegistry mockDiscountRegistry = new MockDiscountRegistry();
        mockCreator = new MockRegistryCreator(mockItemRegistry, mockDiscountRegistry);
        mockPrinter = new MockPrinter();
        controller = new Controller(mockCreator);
    }
    
    @Test
    public void testStartNewSale() {
        controller.startNewSale();
        assertNotNull("Sale should be started", controller.getCurrentSale());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEnterItemWithoutStartingSale() {
        controller.enterItem("1", 1);
    }
    
    @Test
    public void testEnterValidItem() {
        controller.startNewSale();
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);
        
        assertNotNull("Result should not be null for valid item", result);
        assertEquals("Item ID should match", "1", result.getItem().getItemID());
        assertFalse("First entry should not be a duplicate", result.isDuplicate());
        assertEquals("Running total should be calculated correctly", new Amount(62.5), result.getRunningTotal());
    }
    
    @Test
    public void testEnterInvalidItem() {
        controller.startNewSale();
        Controller.ItemWithRunningTotal result = controller.enterItem("999", 1);
        
        assertNull("Result should be null for invalid item", result);
    }
    
    @Test
    public void testEnterDuplicateItem() {
        controller.startNewSale();
        controller.enterItem("1", 1);
        Controller.ItemWithRunningTotal result = controller.enterItem("1", 1);
        
        assertTrue("Second entry of same item should be marked as duplicate", result.isDuplicate());
    }
    
    @Test
    public void testEndSale() {
        controller.startNewSale();
        controller.enterItem("1", 2); // 50.0 * 2 = 100.0, VAT = 25.0
        controller.enterItem("2", 1); // 30.0 * 1 = 30.0, VAT = 3.6
        
        Amount total = controller.endSale();
        Amount expected = new Amount(158.6); // 100.0 + 30.0 + 25.0 + 3.6
        
        assertEquals("End sale should return correct total", expected, total);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEndSaleWithoutStartingSale() {
        controller.endSale();
    }
    
    @Test
    public void testRequestDiscount() {
        controller.startNewSale();
        controller.enterItem("1", 1); // 50.0 + 12.5 VAT = 62.5
        
        Amount totalAfterDiscount = controller.requestDiscount("1001");
        Amount expected = new Amount(52.5); // 62.5 - 10.0
        
        assertEquals("Discount should be applied correctly", expected, totalAfterDiscount);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRequestDiscountWithoutStartingSale() {
        controller.requestDiscount("1001");
    }
    
    @Test
    public void testPay() {
        controller.startNewSale();
        controller.enterItem("1", 1); // 50.0 + 12.5 VAT = 62.5
        
        Amount change = controller.pay(new Amount(100.0));
        Amount expectedChange = new Amount(37.5); // 100.0 - 62.5
        
        assertEquals("Change should be calculated correctly", expectedChange, change);
        assertTrue("Inventory should be updated", mockItemRegistry.wasInventoryUpdated());
        assertTrue("Receipt should be printed", mockPrinter.wasReceiptPrinted());
    }
    
    @Test
    public void testExternalSystemObserver() {
        MockExternalSystemObserver observer = new MockExternalSystemObserver();
        controller.addExternalSystemObserver(observer);
        
        controller.startNewSale();
        controller.enterItem("1", 1);
        controller.pay(new Amount(100.0));
        
        assertTrue("External system observer should be notified", observer.wasNotified());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testPayWithoutStartingSale() {
        controller.pay(new Amount(100.0));
    }
}