package se.kth.iv1350.pos.integration;

import se.kth.iv1350.pos.model.Sale;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external accounting system.
 * This class is responsible for sending sales information for accounting purposes,
 * recording sales data, and updating sales statistics.
 */
public class AccountingSystem {
    /**
     * Creates a new instance with a connection to the accounting system.
     */
    public AccountingSystem() {
    }

    /**
     * Records a completed sale in the external accounting system.
     * Transmits sale information for financial record-keeping.
     *
     * @param sale The completed sale to record.
     */
    public void recordSale(Sale sale) {
        Amount totalPrice = sale.calculateTotal();
        Amount totalVAT = sale.calculateTotalVat();

        logSaleRecorded(totalPrice, totalVAT);
    }

    /**
     * Updates daily sales statistics in the accounting system.
     * This allows for real-time sales performance tracking.
     *
     * @param saleAmount The total amount from the completed sale.
     */
    public void updateSalesStatistics(Amount saleAmount) {
        logStatisticsUpdated(saleAmount);
    }

    private void logSaleRecorded(Amount totalPrice, Amount totalVAT) {
        System.out.println("Sale recorded in accounting system:");
        System.out.println("  Total amount: " + totalPrice);
        System.out.println("  Total VAT: " + totalVAT);
    }

    private void logStatisticsUpdated(Amount saleAmount) {
        System.out.println("Sales statistics updated. Amount: " + saleAmount);
    }
}
