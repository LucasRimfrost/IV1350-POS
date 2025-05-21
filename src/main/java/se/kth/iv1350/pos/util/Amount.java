package se.kth.iv1350.pos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents an immutable amount of money.
 * Provides utility methods for arithmetic operations and comparisons,
 * ensuring consistent precision and rounding behavior.
 */
public final class Amount {
    private final BigDecimal value;

    /**
     * Creates a new instance representing the specified monetary amount using {@code BigDecimal}.
     * The value is scaled to two decimal places with half-up rounding.
     *
     * @param value The amount to represent.
     */
    public Amount(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Creates a new instance representing the specified monetary amount using a {@code double}.
     * The value is internally converted to a {@code BigDecimal} and scaled to two decimal places.
     *
     * @param value The amount to represent.
     */
    public Amount(double value) {
        this(BigDecimal.valueOf(value));
    }

    /**
     * Creates a new instance representing zero monetary value.
     */
    public Amount() {
        this(0.0);
    }

    /**
     * Adds the specified amount to this amount.
     *
     * @param other The amount to add.
     * @return A new {@code Amount} representing the sum of the two amounts.
     */
    public Amount add(Amount other) {
        return new Amount(this.value.add(other.value));
    }

    /**
     * Subtracts the specified amount from this amount.
     *
     * @param other The amount to subtract.
     * @return A new {@code Amount} representing the result of the subtraction.
     */
    public Amount subtract(Amount other) {
        return new Amount(this.value.subtract(other.value));
    }

    /**
     * Multiplies this amount by the specified factor.
     *
     * @param factor The multiplier.
     * @return A new {@code Amount} representing the product.
     */
    public Amount multiply(double factor) {
        return new Amount(this.value.multiply(BigDecimal.valueOf(factor)));
    }

    /**
     * Returns the numerical value of this amount.
     *
     * @return The monetary value as a {@code BigDecimal}.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Checks whether the amount is greater than zero.
     *
     * @return {@code true} if the amount is positive, {@code false} otherwise.
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns a string representation of the amount in SEK with two decimal precision.
     *
     * @return A formatted string with the amount followed by "SEK".
     */
    @Override
    public String toString() {
        return String.format("%.2f SEK", value.doubleValue());
    }

    /**
     * Checks if this amount is equal to another object.
     * Two {@code Amount} objects are considered equal if their values are the same.
     *
     * @param obj The object to compare to.
     * @return {@code true} if the object is an {@code Amount} with the same value, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Amount)) {
            return false;
        }
        Amount other = (Amount) obj;
        return this.value.equals(other.value);
    }
}
