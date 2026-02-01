package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

    class FixedDiscountTest {

        @Test
        @DisplayName("Fast rabatt på 20 kr ska minska priset från 100 kr till 80 kr")
        void apply_20krDiscount_shouldReducePrice() {
            Discount discount = new FixedDiscount(20.0);
            double result = discount.apply(100.0);
            assertThat(result).isEqualTo(80.0);
        }
    }