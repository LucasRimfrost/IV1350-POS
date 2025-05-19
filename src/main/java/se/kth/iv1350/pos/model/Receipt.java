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
    private final int AMOUNT_COLUMN = 40;

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

        appendReceiptHeader(receipt);
        appendItemDetails(receipt);
        appendTotals(receipt);
        appendPaymentDetails(receipt);
        appendReceiptFooter(receipt);

        return receipt.toString();
    }

    private void appendReceiptHeader(StringBuilder receipt) {
        receipt.append("------------------ Begin receipt -------------------\n");
        receipt.append("Time of Sale : ").append(formatDateTime(saleTime)).append("\n\n");
    }

    private void appendItemDetails(StringBuilder receipt) {
        for (SaleLineItem lineItem : sale.getItems()) {
            String leftSide = String.format("%s %d x %s",
                    lineItem.getItem().getName(),
                    lineItem.getQuantity(),
                    formatAmount(lineItem.getItem().getPrice()));
            receipt.append(formatLineWithAmount(leftSide, lineItem.getSubtotal())).append("\n");
        }
        receipt.append("\n");
    }

    private void appendTotals(StringBuilder receipt) {
        receipt.append(formatLineWithAmount("Total :", sale.calculateTotalWithVat())).append("\n");
        receipt.append(formatLineWithAmount("VAT :", sale.calculateTotalVat(), false)).append("\n\n");
    }

    private void appendPaymentDetails(StringBuilder receipt) {
        receipt.append(formatLineWithAmount("Cash :", paymentAmount)).append("\n");
        receipt.append(formatLineWithAmount("Change :", changeAmount)).append("\n");
    }

    private void appendReceiptFooter(StringBuilder receipt) {
        receipt.append("------------------ End receipt ---------------------");
    }

    private String formatLineWithAmount(String leftText, Amount amount) {
        return formatLineWithAmount(leftText, amount, true);
    }

    private String formatLineWithAmount(String leftText, Amount amount, boolean includeSEK) {
        StringBuilder line = new StringBuilder(leftText);
        String amountStr = formatAmount(amount);

        int spacesNeeded = AMOUNT_COLUMN - line.length() - amountStr.length();
        if (spacesNeeded < 1) spacesNeeded = 1;

        line.append(" ".repeat(spacesNeeded)).append(amountStr);

        if (includeSEK) {
            line.append(" SEK");
        }

        return line.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    private String formatAmount(Amount amount) {
        return String.format("%.2f", amount.getValue().doubleValue()).replace('.', ':');
    }
}
