package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
}