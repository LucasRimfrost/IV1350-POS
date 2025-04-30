package se.kth.iv1350.pos.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.kth.iv1350.pos.dto.ItemDTO;
import se.kth.iv1350.pos.model.SaleLineItem;
import se.kth.iv1350.pos.util.Amount;

/**
 * Contains all calls to the external discount database.
 * This class is responsible for retrieving and calculating discount information.
 */
public class DiscountRegistry {
    private Map<String, Double> customerDiscounts = new HashMap<>();
    private Map<String, Double> itemDiscounts = new HashMap<>();
    private Map<String, Set<String>> itemCombinationDiscounts = new HashMap<>();
    private Map<String, Double> combinationDiscountRates = new HashMap<>();

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
        Amount totalDiscount = new Amount(0);
        
        // Apply customer-specific discount
        Amount customerDiscount = calculateCustomerDiscount(totalAmount, customerID);
        totalDiscount = totalDiscount.add(customerDiscount);
        
        // Apply volume-based discount
        Amount volumeDiscount = calculateVolumeDiscount(totalAmount);
        totalDiscount = totalDiscount.add(volumeDiscount);
        
        // Apply item-specific discounts
        Amount itemDiscount = calculateItemSpecificDiscounts(items);
        totalDiscount = totalDiscount.add(itemDiscount);
        
        // Apply item combination discounts
        Amount combinationDiscount = calculateItemCombinationDiscounts(items);
        totalDiscount = totalDiscount.add(combinationDiscount);
        
        // Log discount details for accounting purposes
        logDiscountDetails(customerDiscount, volumeDiscount, itemDiscount, combinationDiscount, totalDiscount);
        
        return totalDiscount;
    }
    
    /**
     * Verifies if a customer exists in the discount registry.
     *
     * @param customerID The customer identifier.
     * @return true if the customer exists, false otherwise.
     */
    public boolean customerExists(String customerID) {
        return customerDiscounts.containsKey(customerID);
    }
    
    /**
     * Gets the discount rate for a specific customer.
     *
     * @param customerID The customer identifier.
     * @return The customer's discount rate as a decimal, or 0 if no discount exists.
     */
    public double getCustomerDiscountRate(String customerID) {
        Double discountRate = customerDiscounts.get(customerID);
        return discountRate != null ? discountRate : 0;
    }
    
    /**
     * Gets the discount rate for a specific item.
     *
     * @param itemID The item identifier.
     * @return The item's discount rate as a decimal, or 0 if no discount exists.
     */
    public double getItemDiscountRate(String itemID) {
        Double discountRate = itemDiscounts.get(itemID);
        return discountRate != null ? discountRate : 0;
    }

    private Amount calculateCustomerDiscount(Amount totalAmount, String customerID) {
        Double customerDiscountPercent = customerDiscounts.get(customerID);
        
        if (customerDiscountPercent != null) {
            return totalAmount.multiply(customerDiscountPercent);
        }
        
        return new Amount(0);
    }
    
    private Amount calculateVolumeDiscount(Amount totalAmount) {
        if (totalAmount.getValue().doubleValue() > 1000) {
            return totalAmount.multiply(0.03); // 3% for purchases over 1000
        } else if (totalAmount.getValue().doubleValue() > 500) {
            return totalAmount.multiply(0.02); // 2% for purchases over 500
        }
        
        return new Amount(0);
    }
    
    private Amount calculateItemSpecificDiscounts(List<SaleLineItem> items) {
        Amount discount = new Amount(0);
        
        for (SaleLineItem item : items) {
            ItemDTO itemDTO = item.getItem();
            String itemID = itemDTO.getItemID();
            Double discountRate = itemDiscounts.get(itemID);
            
            if (discountRate != null) {
                Amount itemPrice = itemDTO.getPrice().multiply(item.getQuantity());
                Amount itemDiscount = itemPrice.multiply(discountRate);
                discount = discount.add(itemDiscount);
                
                System.out.println("Applied item discount for " + itemDTO.getDescription() + 
                                 " (" + discountRate * 100 + "%): " + itemDiscount);
            }
        }
        
        return discount;
    }
    
    private Amount calculateItemCombinationDiscounts(List<SaleLineItem> items) {
        Amount discount = new Amount(0);
        Set<String> saleItemIDs = new HashSet<>();
        
        // Extract all item IDs in the sale
        for (SaleLineItem item : items) {
            saleItemIDs.add(item.getItem().getItemID());
        }
        
        // Check for matching combinations
        for (Map.Entry<String, Set<String>> entry : itemCombinationDiscounts.entrySet()) {
            String combinationID = entry.getKey();
            Set<String> requiredItems = entry.getValue();
            
            // Check if all required items are in the sale
            if (saleItemIDs.containsAll(requiredItems)) {
                Double discountRate = combinationDiscountRates.get(combinationID);
                if (discountRate != null) {
                    // Calculate total price of the combination items
                    Amount combinationPrice = new Amount(0);
                    for (SaleLineItem item : items) {
                        if (requiredItems.contains(item.getItem().getItemID())) {
                            Amount itemPrice = item.getItem().getPrice().multiply(item.getQuantity());
                            combinationPrice = combinationPrice.add(itemPrice);
                        }
                    }
                    
                    Amount combinationDiscount = combinationPrice.multiply(discountRate);
                    discount = discount.add(combinationDiscount);
                    
                    System.out.println("Applied combination discount for combination " + combinationID + 
                                     " (" + discountRate * 100 + "%): " + combinationDiscount);
                }
            }
        }
        
        return discount;
    }
    
    private void logDiscountDetails(Amount customerDiscount, Amount volumeDiscount, 
                                  Amount itemDiscount, Amount combinationDiscount, Amount totalDiscount) {
        System.out.println("Discount details:");
        System.out.println("  Customer discount: " + customerDiscount);
        System.out.println("  Volume discount: " + volumeDiscount);
        System.out.println("  Item-specific discounts: " + itemDiscount);
        System.out.println("  Combination discounts: " + combinationDiscount);
        System.out.println("  Total discount: " + totalDiscount);
    }

    private void addTestDiscounts() {
        // Customer discounts
        customerDiscounts.put("1001", 0.10); // 10% discount for customer 1001
        customerDiscounts.put("1002", 0.15); // 15% discount for customer 1002
        
        // Item-specific discounts
        itemDiscounts.put("1", 0.05);  // 5% discount on Apples
        itemDiscounts.put("5", 0.10);  // 10% discount on Cheese
        
        // Item combination discounts
        // Combination 1: Bread and Cheese
        Set<String> combination1 = new HashSet<>();
        combination1.add("4"); // Bread
        combination1.add("5"); // Cheese
        itemCombinationDiscounts.put("COMBO1", combination1);
        combinationDiscountRates.put("COMBO1", 0.15); // 15% discount on bread and cheese together
        
        // Combination 2: Apple, Orange, and Milk
        Set<String> combination2 = new HashSet<>();
        combination2.add("1"); // Apple
        combination2.add("2"); // Orange
        combination2.add("3"); // Milk
        itemCombinationDiscounts.put("COMBO2", combination2);
        combinationDiscountRates.put("COMBO2", 0.20); // 20% discount on fruit and milk together
    }
}
