package se.kth.iv1350.pos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents a receipt, which proves that a payment has been made.
 */
public class Receipt {
    private final Sale sale;
    private final LocalDateTime saleTime;
    private final Amount paymentAmount;
    private final Amount changeAmount;

    /**
     * Creates a new instance.
     *
     * @param sale The sale proved by this receipt.
     * @param paymentAmount The amount paid by the customer.
     * @param changeAmount The amount of change returned to the customer.
     */
    public Receipt(Sale sale, Amount paymentAmount, Amount changeAmount) {
        this.sale = sale;
        this.saleTime = LocalDateTime.now();
        this.paymentAmount = paymentAmount;
        this.changeAmount = changeAmount;
    }

    /**
     * Creates a formatted string with the entire receipt.
     *
     * @return The formatted receipt.
     */
    public String format() {
        StringBuilder receipt = new StringBuilder();

        // Header
        receipt.append("===== RECEIPT =====\n");
        receipt.append(formatDateTime(saleTime)).append("\n\n");

        // Items
        receipt.append("ITEMS:\n");
        for (SaleLineItem lineItem : sale.getItems()) {
            receipt.append(formatLineItem(lineItem)).append("\n");
        }
        receipt.append("\n");

        // Totals
        receipt.append("Subtotal: ").append(sale.calculateTotal()).append("\n");
        receipt.append("VAT: ").append(sale.calculateTotalVat()).append("\n");

        if (sale.hasDiscount()) {
            receipt.append("Discount: -").append(sale.getDiscountAmount()).append("\n");
        }

        receipt.append("TOTAL: ").append(sale.calculateTotalWithVat()).append("\n\n");

        // Payment
        receipt.append("Paid amount: ").append(paymentAmount).append("\n");
        receipt.append("Change: ").append(changeAmount).append("\n");

        receipt.append("===================\n");
        receipt.append("Thank you for shopping with us!");

        return receipt.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    private String formatLineItem(SaleLineItem lineItem) {
        return String.format("%-20s %3d × %8s = %8s",
                lineItem.getItem().getDescription(),
                lineItem.getQuantity(),
                lineItem.getItem().getPrice(),
                lineItem.getSubtotal());
    }
}
