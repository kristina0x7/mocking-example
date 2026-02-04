package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PercentageDiscountTest {

    @Test
    void constructor_negativePercentage_throws() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PercentageDiscount(-5)
        );
        assertEquals("Percentage must be between 0 and 100", exception.getMessage());
    }

    @Test
    void constructor_percentageAbove100_throws() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new PercentageDiscount(150)
        );
        assertEquals("Percentage must be between 0 and 100", exception.getMessage());
    }

    @Test
    void apply_reducesPriceCorrectly() {
        PercentageDiscount discount = new PercentageDiscount(10);
        double original = 2000.0;
        assertEquals(1800.0, discount.apply(original), 0.001);
    }

    @Test
    void apply_zeroPercentage_doesNothing() {
        PercentageDiscount discount = new PercentageDiscount(0);
        double original = 1500.0;
        assertEquals(1500.0, discount.apply(original), 0.001);
    }

    @Test
    void apply_100Percentage_returnsZero() {
        PercentageDiscount discount = new PercentageDiscount(100);
        double original = 1500.0;
        assertEquals(0.0, discount.apply(original), 0.001);
    }
}
