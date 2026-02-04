package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

    class PercentageDiscountTest {

        @Test
        void apply_10PercentDiscount_shouldReducePrice() {
        Discount discount = new PercentageDiscount(10.0);
        double result = discount.apply(100.0);
        assertThat(result).isEqualTo(90.0);
    }
}