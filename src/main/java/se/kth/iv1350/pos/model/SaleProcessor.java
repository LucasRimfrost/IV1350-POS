package se.kth.iv1350.pos.model;

import se.kth.iv1350.pos.dto.ReceiptDTO;
import se.kth.iv1350.pos.dto.SaleDTO;
import se.kth.iv1350.pos.dto.SaleItemDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain service that handles operations spanning multiple domain objects.
 * Responsible for transforming model objects to DTOs and vice versa.
 */
public class SaleProcessor {
    /**
     * Creates a DTO from a Sale object.
     *
     * @param sale The sale to convert
     * @return A DTO representing the sale
     */
    public SaleDTO createSaleDTO(Sale sale) {
        if (sale == null) {
            return null;
        }

        List<SaleItemDTO> itemDTOs = new ArrayList<>();
        for (SaleLineItem item : sale.getItems()) {
            itemDTOs.add(new SaleItemDTO(
                item.getItem(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getVatAmount(),
                item.getTotalWithVat()
            ));
        }

        return new SaleDTO(
            itemDTOs,
            sale.calculateTotal(),
            sale.calculateTotalVat(),
            sale.getDiscountAmount(),
            sale.calculateTotalWithVat(),
            sale.getSaleTime()
        );
    }

    /**
     * Creates a Receipt DTO from a Receipt object.
     *
     * @param receipt The receipt to convert
     * @return A DTO representing the receipt
     */
    public ReceiptDTO createReceiptDTO(Receipt receipt) {
        if (receipt == null) {
            return null;
        }

        List<SaleItemDTO> itemDTOs = new ArrayList<>();
        for (SaleLineItem item : receipt.getItems()) {
            itemDTOs.add(new SaleItemDTO(
                item.getItem(),
                item.getQuantity(),
                item.getSubtotal(),
                item.getVatAmount(),
                item.getTotalWithVat()
            ));
        }

        return new ReceiptDTO(
            itemDTOs,
            receipt.getTotalAmount(),
            receipt.getTotalVat(),
            receipt.getPaymentAmount(),
            receipt.getChangeAmount(),
            receipt.getSaleTime()
        );
    }
}
