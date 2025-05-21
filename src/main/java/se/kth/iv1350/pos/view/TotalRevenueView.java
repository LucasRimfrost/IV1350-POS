package se.kth.iv1350.pos.view;

import se.kth.iv1350.pos.model.SaleObserver;
import se.kth.iv1350.pos.util.Amount;

/**
 * Shows the total income from all sales on the user interface.
 */
public class TotalRevenueView implements SaleObserver {
    private Amount totalRevenue = new Amount();

    /**
     * Creates a new instance.
     */
    public TotalRevenueView() {
        totalRevenue = new Amount();
    }

    /**
     * Displays the running total revenue whenever a new sale is made.
     *
     * @param totalPaid The amount paid for the sale that was just completed
     */
    @Override
    public void newSale(Amount totalPaid) {
        totalRevenue = totalRevenue.add(totalPaid);
        printCurrentRevenue();
    }

    private void printCurrentRevenue() {
        System.out.println();
        System.out.println("*** TOTAL REVENUE DISPLAY ***");
        System.out.println("Total revenue since program start: " + totalRevenue + " SEK");
        System.out.println("*****************************");
        System.out.println();
    }
}
