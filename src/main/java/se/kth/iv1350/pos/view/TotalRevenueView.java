package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.util.Amount;

/**
 * Displays the total revenue from all sales on the user interface.
 * This class is an observer that gets notified when sales are completed.
 */
public class TotalRevenueView implements SaleObserver {
    private Amount totalRevenue;

    /**
     * Creates a new instance with zero initial revenue.
     */
    public TotalRevenueView() {
        this.totalRevenue = new Amount();
    }

    /**
     * Updates the total revenue when a new sale is completed.
     * Displays the current total revenue on the user interface.
     *
     * @param saleAmount The amount of the completed sale.
     */
    @Override
    public void newSaleCompleted(Amount saleAmount) {
        totalRevenue = totalRevenue.add(saleAmount);
        printCurrentRevenue();
    }

    /**
     * Prints the current total revenue to the console.
     */
    private void printCurrentRevenue() {
        System.out.println();
        System.out.println("*** Total Revenue Display ***");
        System.out.println("Total revenue since program start: " + totalRevenue);
        System.out.println("*****************************");
    }
}
