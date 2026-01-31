package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Ny varukorg ska vara tom")
    void newCart_ShouldBeEmpty(){
        assertThat(cart.isEmpty()).istrue();
        assertThat(cart.getItemCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ny varukorg ska ha totalpris 0")
    void newCart_shouldHaveZeroTotalPrice() {
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }
}