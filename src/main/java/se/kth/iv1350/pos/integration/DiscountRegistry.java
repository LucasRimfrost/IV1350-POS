package se.kth.iv1350.pos.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external discount database.
 * This class is responsible for retrieving discount information.
 */
public class DiscountRegistry {
    private Map<String, Double> customerDiscounts = new HashMap<>();

    /**
     * Creates a new instance and initializes with some default discounts.
     */
    public DiscountRegistry() {
        addTestDiscounts();
    }

    /**
     * Calculates the discount based on sale items, total amount, and customer ID.
     * Implementation matches the requirements from the specification.
     *
     * @param items The items in the sale.
     * @param totalAmount The total amount of the sale.
     * @param customerID The customer identifier.
     * @return The discount amount.
     */
    public Amount getDiscount(List<SaleLineItem> items, Amount totalAmount, String customerID) {
        // This would contain more complex logic in a real implementation

        // First check for customer-specific discount
        Amount discount = new Amount(0);
        Double customerDiscountPercent = customerDiscounts.get(customerID);

        if (customerDiscountPercent != null) {
            Amount customerDiscount = totalAmount.multiply(customerDiscountPercent);
            discount = discount.add(customerDiscount);
        }

        // Check for total price-based discount
        if (totalAmount.getValue().doubleValue() > 1000) {
            Amount volumeDiscount = totalAmount.multiply(0.03); // 3% for purchases over 1000
            discount = discount.add(volumeDiscount);
        }

        // Special item combination discounts would be calculated here
        // This is simplified for this implementation

        return discount;
    }

    private void addTestDiscounts() {
        customerDiscounts.put("1001", 0.10); // 10% discount for customer 1001
        customerDiscounts.put("1002", 0.15); // 15% discount for customer 1002
    }
}
