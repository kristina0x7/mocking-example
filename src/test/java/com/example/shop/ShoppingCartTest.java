package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    private ShoppingCart cart;
    private Product cpu;
    private Product ram;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        cpu = new Product(UUID.randomUUID(), "CPU", 3700.0);
        ram = new Product(UUID.randomUUID(), "RAM", 4300.0);
    }

    @Nested
    class EmptyCart {

        @Test
        void newCart_isEmpty() {
            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getItemCount());
            assertNull(cart.getDiscount());
        }

        @Test
        void isEmpty_returnsFalseWhenCartHasItems() {
            cart.addProduct(cpu, 1);
            assertFalse(cart.isEmpty());
        }
    }


    @Nested
    class AddingProducts {

        @Test
        void addProduct_increasesItemCount() {
            cart.addProduct(cpu, 2);
            assertEquals(2, cart.getItemCount());
        }

        @Test
        void addProduct_existingProduct_sumsQuantities() {
            cart.addProduct(cpu, 2);
            cart.addProduct(cpu, 3);
            assertEquals(5, cart.getQuantity(cpu.getId()));
            assertEquals(5, cart.getItemCount());
        }
    }


    @Nested
    class RemoveProducts {

        @Test
        void removeProduct_reducesQuantity() {
            cart.addProduct(cpu, 5);
            cart.removeProduct(cpu.getId(), 2);
            assertEquals(3, cart.getQuantity(cpu.getId()));
        }

        @Test
        void removeProduct_exactQuantity_removesItem() {
            cart.addProduct(cpu, 3);
            cart.removeProduct(cpu.getId(), 3);
            assertFalse(cart.containsProduct(cpu.getId()));
            assertEquals(0, cart.getItemCount());
        }
    }


    @Nested
    class Discounts {

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
        void getDiscount_returnsSetDiscount() {
            FixedDiscount discount = new FixedDiscount(100);
            cart.setDiscount(discount);
            assertEquals(discount, cart.getDiscount());
        }
    }


    @Nested
    class CartContents {

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
        void containsProduct_returnsTrueIfPresent() {
            cart.addProduct(cpu, 1);
            assertTrue(cart.containsProduct(cpu.getId()));
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
    }


    @Nested
    class Validation {

        @Test
        void addProduct_nullProduct_throwsNullPointerException() {
            NullPointerException ex = assertThrows(
                    NullPointerException.class,
                    () -> cart.addProduct(null, 1)
            );
            assertEquals("Product cannot be null", ex.getMessage());
        }

        @Test
        void addProduct_nonPositiveQuantity_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(cpu, 0)
            );
            assertEquals("Quantity must be positive", ex.getMessage());
        }

        @Test
        void removeProduct_nonPositiveQuantity_throwsIllegalArgumentException() {
            cart.addProduct(cpu, 1);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.removeProduct(cpu.getId(), 0)
            );
            assertEquals("Quantity must be positive", ex.getMessage());
        }

        @Test
        void removeProduct_productNotInCart_throwsCartException() {
            CartException ex = assertThrows(
                    CartException.class,
                    () -> cart.removeProduct(ram.getId(), 1)
            );
            assertEquals(
                    "Product not found in cart: " + ram.getId(),
                    ex.getMessage()
            );
        }
    }
}