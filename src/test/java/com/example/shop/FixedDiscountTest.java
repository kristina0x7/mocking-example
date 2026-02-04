package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedDiscountTest {

    @Test
    void constructor_negativeAmount_throws() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FixedDiscount(-100)
        );
        assertEquals("Discount amount cannot be negative", exception.getMessage());
    }

    @Test
    void apply_reducesPriceCorrectly() {
        FixedDiscount discount = new FixedDiscount(300);
        double original = 2000.0;
        assertEquals(1700.0, discount.apply(original), 0.001);
    }

    @Test
    void apply_doesNotGoBelowZero() {
        FixedDiscount discount = new FixedDiscount(2500);
        double original = 2000.0;
        assertEquals(0.0, discount.apply(original), 0.001);
    }
}
