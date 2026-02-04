package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
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

    @Nested
    class AddQuantity {

        @Test
        void addQuantity_increasesQuantity() {
            CartItem item = new CartItem(cpu, 2);
            item.addQuantity(3);
            assertEquals(5, item.getQuantity());
        }

        @Test
        void addQuantity_negative_throws() {
            CartItem item = new CartItem(cpu, 2);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.addQuantity(-1)
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }

        @Test
        void addQuantity_zero_throws() {
            CartItem item = new CartItem(cpu, 2);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.addQuantity(0)
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }
    }

    @Nested
    class RemoveQuantity {

        @Test
        void removeQuantity_reducesQuantity() {
            CartItem item = new CartItem(cpu, 5);
            item.removeQuantity(2);
            assertEquals(3, item.getQuantity());
        }

        @Test
        void removeQuantity_toZero() {
            CartItem item = new CartItem(cpu, 3);
            item.removeQuantity(3);
            assertEquals(0, item.getQuantity());
            assertTrue(item.isEmpty());
        }

        @Test
        void removeQuantity_exceedQuantity_setsZero() {
            CartItem item = new CartItem(cpu, 2);
            item.removeQuantity(5);
            assertEquals(0, item.getQuantity());
            assertTrue(item.isEmpty());
        }

        @Test
        void removeQuantity_negative_throws() {
            CartItem item = new CartItem(cpu, 3);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.removeQuantity(-2)
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }

        @Test
        void removeQuantity_zero_throws() {
            CartItem item = new CartItem(cpu, 3);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> item.removeQuantity(0)
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }
    }
}