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
}