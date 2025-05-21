package se.kth.iv1350.pos.dto;

import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) for a sale line item.
 */
public record SaleItemDTO(
    ItemDTO item,
    int quantity,
    Amount subtotal,
    Amount vatAmount,
    Amount totalWithVat
) {}
