package se.kth.iv1350.pos.integration;

import java.time.format.DateTimeFormatter;
import se.kth.iv1350.pos.dto.ReceiptDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import se.kth.iv1350.pos.util.Amount;

/**
 * Represents the printer, used for printing receipts.
 */
public class Printer {
    private final int AMOUNT_COLUMN = 40; // Column for right-aligned amounts

    /**
     * Creates a new instance.
     */
    public Printer() {
        // Would initialize printer hardware in a real implementation
    }

    /**
     * Prints the specified receipt.
     *
     * @param receiptDTO The receipt data to print
     */
    public void printReceipt(ReceiptDTO receiptDTO) {
        System.out.println("Printing receipt...");
        System.out.println(formatReceipt(receiptDTO));
    }

    /**
     * Formats the receipt data into a printable string.
     *
     * @param receiptDTO The receipt data to format
     * @return A formatted receipt string
     */
    private String formatReceipt(ReceiptDTO receiptDTO) {
        StringBuilder receipt = new StringBuilder();

        appendReceiptHeader(receipt, receiptDTO);
        appendItemDetails(receipt, receiptDTO);
        appendTotals(receipt, receiptDTO);
        appendPaymentDetails(receipt, receiptDTO);
        appendReceiptFooter(receipt);

        return receipt.toString();
    }

    private void appendReceiptHeader(StringBuilder receipt, ReceiptDTO receiptDTO) {
        receipt.append("------------------ Begin receipt -------------------\n");
        receipt.append("Time of Sale : ").append(formatDateTime(receiptDTO.saleTime())).append("\n\n");
    }

    private void appendItemDetails(StringBuilder receipt, ReceiptDTO receiptDTO) {
        for (SaleItemDTO lineItem : receiptDTO.items()) {
            String leftSide = String.format("%s %d x %s",
                    lineItem.item().name(),
                    lineItem.quantity(),
                    formatAmount(lineItem.item().price()));
            receipt.append(formatLineWithAmount(leftSide, lineItem.subtotal())).append("\n");
        }
        receipt.append("\n");
    }

    private void appendTotals(StringBuilder receipt, ReceiptDTO receiptDTO) {
        Amount totalWithVat = receiptDTO.total().add(receiptDTO.totalVat());
        receipt.append(formatLineWithAmount("Total :", totalWithVat)).append("\n");
        receipt.append(formatLineWithAmount("VAT :", receiptDTO.totalVat())).append("\n\n");
    }

    private void appendPaymentDetails(StringBuilder receipt, ReceiptDTO receiptDTO) {
        receipt.append(formatLineWithAmount("Cash :", receiptDTO.paymentAmount())).append("\n");
        receipt.append(formatLineWithAmount("Change :", receiptDTO.changeAmount())).append("\n");
    }

    private void appendReceiptFooter(StringBuilder receipt) {
        receipt.append("------------------ End receipt ---------------------");
    }

    private String formatLineWithAmount(String leftText, Amount amount) {
        StringBuilder line = new StringBuilder(leftText);
        String amountStr = formatAmount(amount);

        int spacesNeeded = AMOUNT_COLUMN - line.length() - amountStr.length();
        if (spacesNeeded < 1) spacesNeeded = 1;

        line.append(" ".repeat(spacesNeeded)).append(amountStr).append(" SEK");

        return line.toString();
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    private String formatAmount(Amount amount) {
        return String.format("%.2f", amount.getValue().doubleValue()).replace('.', ':');
    }
}
