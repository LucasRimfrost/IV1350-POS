package se.kth.iv1350.pos.dto;

import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) for item information.
 * This record is purely a data container without business logic.
 */
public record ItemDTO(
    String itemID,
    String name,
    String description,
    Amount price,
    double vatRate
) {}
