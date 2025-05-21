package se.kth.iv1350.pos.dto;

import java.time.LocalDateTime;
import java.util.List;
import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) containing sale information.
 */
public record SaleDTO(
    List<SaleItemDTO> items,
    Amount total,
    Amount totalVat,
    Amount discountAmount,
    Amount totalWithVat,
    LocalDateTime saleTime
) {}
