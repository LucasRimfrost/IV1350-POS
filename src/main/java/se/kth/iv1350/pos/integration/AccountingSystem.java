package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external accounting system.
 */
public class AccountingSystem {
    private static final AccountingSystem instance = new AccountingSystem();

    /**
     * Gets the singleton instance of the AccountingSystem.
     *
     * @return The singleton instance.
     */
    public static AccountingSystem getInstance() {
        return instance;
    }

    /**
     * Records a completed sale in the external accounting system.
     *
     * @param saleDTO The sale data to record
     */
    public void recordSale(SaleDTO saleDTO) {
        System.out.println("Sale recorded in accounting system:");
        System.out.println("  Total amount: " + saleDTO.total());
        System.out.println("  Total VAT: " + saleDTO.totalVat());
    }

    /**
     * Updates daily sales statistics in the accounting system.
     *
     * @param saleAmount The total amount from the sale
     */
    public void updateSalesStatistics(Amount saleAmount) {
        System.out.println("Sales statistics updated. Amount: " + saleAmount);
    }

    private AccountingSystem() {
    }
}
