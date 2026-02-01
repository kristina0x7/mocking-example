package com.example.shop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

    class FixedDiscountTest {

        @Test
        void apply_20krDiscount_shouldReducePrice() {
            Discount discount = new FixedDiscount(20.0);
            double result = discount.apply(100.0);
            assertThat(result).isEqualTo(80.0);
        }
    }