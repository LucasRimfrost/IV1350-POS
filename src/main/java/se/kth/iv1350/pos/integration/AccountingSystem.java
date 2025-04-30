package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external accounting system.
 * This class is responsible for sending sales information for accounting purposes.
 */
public class AccountingSystem {
    
    /**
     * Creates a new instance.
     */
    public AccountingSystem() {
        // Real implementation would initialize accounting system integration
    }
    
    /**
     * Records a completed sale in the accounting system.
     * This would connect to the external accounting system in a production environment.
     *
     * @param sale The sale to record.
     */
    public void recordSale(Sale sale) {
        // This would call the external accounting system in a real implementation
        Amount totalPrice = sale.calculateTotal();
        Amount totalVAT = sale.calculateTotalVat();
        
        System.out.println("Sale recorded in accounting system:");
        System.out.println("  Total amount: " + totalPrice);
        System.out.println("  Total VAT: " + totalVAT);
    }
    
    /**
     * Updates daily sales statistics in the accounting system.
     * 
     * @param saleAmount The amount from the completed sale.
     */
    public void updateSalesStatistics(Amount saleAmount) {
        // This would update the external accounting system statistics in a real implementation
        System.out.println("Sales statistics updated. Amount: " + saleAmount);
    }
}