package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    private ShoppingCart cart;
    private Product cpu;
    private Product ram;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        cpu = new Product("CPU", 3700.0);
        ram = new Product("RAM", 4300.0); }

    @Test
    void newCart_isEmpty() {
        assertTrue(cart.isEmpty());
    }

    @Test
    void addProduct_increasesItemCount() {
        cart.addProduct(cpu, 2);
        assertEquals(2, cart.getItemCount());
    }

    @Test
    void removeProduct_reducesQuantity() {
        cart.addProduct(cpu, 5);
        cart.removeProduct(cpu.getId(), 2);
        assertEquals(3, cart.getQuantity(cpu.getId()));
    }

    @Test
    void fixedDiscount_getTotalPrice() {
        cart.addProduct(cpu, 2);
        cart.addProduct(ram, 3);
        FixedDiscount discount = new FixedDiscount(3000);
        cart.setDiscount(discount);
        assertEquals(17300.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    void percentageDiscount_getTotalPrice() {
        cart.addProduct(cpu, 2);
        cart.addProduct(ram, 3);
        PercentageDiscount discount = new PercentageDiscount(10);
        cart.setDiscount(discount);
        assertEquals(18270.0, cart.getTotalPrice(), 0.001);
    }

    @Test
    void containsProduct_returnsTrueIfPresent() {
        cart.addProduct(cpu, 1);
        assertTrue(cart.containsProduct(cpu.getId()));
    }

    @Test
    void clear_shouldEmptyCart() {
        cart.addProduct(cpu, 2);
        cart.addProduct(ram, 1);
        cart.setDiscount(new FixedDiscount(100));
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItemCount());
        assertNull(cart.getDiscount());
    }

    @Test
    void getItems_shouldReturnAllItems() {
        cart.addProduct(cpu, 2);
        cart.addProduct(ram, 1);
        Collection<CartItem> items = cart.getItems();
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(item -> item.getProduct().equals(cpu)));
        assertTrue(items.stream().anyMatch(item -> item.getProduct().equals(ram)));
    }

    @Test
    void getItems_shouldReturnUnmodifiableCollection() {
        cart.addProduct(cpu, 1);
        Collection<CartItem> items = cart.getItems();
        assertThrows(UnsupportedOperationException.class, () -> items.clear());
    }


    @Nested
    class Validation {

        @Test
        void addProduct_nullProduct_throws() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> cart.addProduct(null, 1)
            );
            assertEquals("Product cannot be null", exception.getMessage());
        }

        @Test
        void addProduct_negativeQuantity_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(cpu, -1)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        void addProduct_zeroQuantity_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(cpu, 0)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        void removeProduct_negativeQuantity_throws() {
            cart.addProduct(cpu, 2);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.removeProduct(cpu.getId(), -1)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        void removeProduct_zeroQuantity_throws() {
            cart.addProduct(cpu, 2);
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.removeProduct(cpu.getId(), 0)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        void removeProduct_productNotInCart_throwsCartException() {
            assertFalse(cart.containsProduct(ram.getId()));
            CartException exception = assertThrows(
                    CartException.class,
                    () -> cart.removeProduct(ram.getId(), 1)
            );
            assertEquals(
                    "Product not found in cart: " + ram.getId(),
                    exception.getMessage()
            );
        }
    }
}