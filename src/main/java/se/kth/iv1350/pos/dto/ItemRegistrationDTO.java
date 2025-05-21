package se.kth.iv1350.pos.dto;

import se.kth.iv1350.pos.util.Amount;

/**
 * Data Transfer Object (DTO) for item registration result.
 */
public record ItemRegistrationDTO(
    ItemDTO item,
    Amount runningTotal,
    Amount runningVat,
    boolean isDuplicate
) {}
