package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("Skapa produkt med giltiga värden ska fungera")
    void createProduct_withValidValues_shouldWork() {

        UUID expectedId = UUID.randomUUID();
        Product product = new Product(expectedId, "Apple", 10.0);

        assertThat(product.getId()).isEqualTo(expectedId);
        assertThat(product.getName()).isEqualTo("Apple");
        assertThat(product.getPrice()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Skapa produkt med null ID ska kasta exception")
    void createProduct_withNullId_shouldThrowException() {
        assertThatThrownBy(() -> new Product((UUID) null, "Apple", 10.0))
                .isInstanceOf(IllegalArgumentException.class);
        .hasMessageContaining("D cannot be null");
    }
}