package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

    class PercentageDiscountTest {

        @Test
        @DisplayName("10 % rabatt ska minska priset från 100 kr till 90 kr")
        void apply_10PercentDiscount_shouldReducePrice() {
        Discount discount = new PercentageDiscount(10.0);
        double result = discount.apply(100.0);
        assertThat(result).isEqualTo(90.0);
    }
}