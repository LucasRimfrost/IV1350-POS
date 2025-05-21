package se.kth.iv1350.pos.dto;

import java.time.LocalDateTime;
import java.util.List;
import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) for receipt information.
 */
public record ReceiptDTO(
    List<SaleItemDTO> items,
    Amount total,
    Amount totalVat,
    Amount paymentAmount,
    Amount changeAmount,
    LocalDateTime saleTime
) {}
