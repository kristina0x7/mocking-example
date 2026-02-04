package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CartItemTest {
    private Product cpu;

    @BeforeEach
    void setUp() {
        cpu = new Product("CPU", 3700.0);
    }

    @Nested
    class Constructor {

        @Test
        void constructor_nullProduct_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new CartItem(null, 1)
            );
            assertEquals("Product cannot be null", exception.getMessage());
        }

        @Test
        void constructor_negativeQuantity_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new CartItem(cpu, -5)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        void constructor_zeroQuantity_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new CartItem(cpu, 0)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }
    }
}