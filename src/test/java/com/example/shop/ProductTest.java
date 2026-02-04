package com.example.shop;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void createProduct_withValidValues_shouldWork() {

        UUID expectedId = UUID.randomUUID();
        Product product = new Product(expectedId, "Apple", 10.0);

        assertThat(product.getId()).isEqualTo(expectedId);
        assertThat(product.getName()).isEqualTo("Apple");
        assertThat(product.getPrice()).isEqualTo(10.0);
    }

    @Test
    void createProduct_withNullId_shouldThrowException() {
        assertThatThrownBy(() -> new Product((UUID) null, "Apple", 10.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id cannot be null");
    }
}