package se.kth.iv1350.pos.dto;

import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) for payment information.
 */
public record PaymentDTO(
    Amount paidAmount,
    Amount changeAmount
) {}
