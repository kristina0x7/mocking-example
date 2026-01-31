package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Skapa produkt med giltiga värden ska fungera")
    void createProduct_withValidValues_shouldWork() {

        Product product = new Product("P001", "Apple", 10.0);

        assertThat(product.getId()).isEqualTo("P001");
        assertThat(product.getName()).isEqualTo("Apple");
        assertThat(product.getPrice()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Skapa produkt med tomt ID ska kasta exception")
    void createProduct_withEmptyId_shouldThrowException() {
        assertThatThrownBy(() -> new Product("", "Apple", 10.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}