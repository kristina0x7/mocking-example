package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Ny varukorg ska vara tom")
    void newCart_ShouldBeEmpty(){
        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getItemCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ny varukorg ska ha totalpris 0")
    void newCart_shouldHaveZeroTotalPrice() {
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Lägg till produkt ska öka antalet i varukorgen")
    void addProduct_shouldIncreaseItemCount() {
        Product apple = new Product("Apple", 10.0);

        cart.addProduct(apple);

        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Lägg till produkt med kvantitet ska lägga till rätt antal")
    void addProductWithQuantity_shouldAddCorrectAmount() {
        Product apple = new Product("Apple", 10.0);

        cart.addProduct(apple, 3);

        assertThat(cart.getItemCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lägga till samma produkt flera gånger ska summera kvantiteter")
    void addSameProductTwice_shouldSumQuantities() {
        Product apple = new Product("Apple", 10.0);

        cart.addProduct(apple, 2);
        cart.addProduct(apple, 3);

        assertThat(cart.getItemCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Hämta kvantitet ska returnera korrekt antal")
    void getQuantity_shouldReturnCorrectAmount() {
        Product apple = new Product("Apple", 10.0);

        cart.addProduct(apple, 3);
        int quantity = cart.getQuantity(apple);

        assertThat(quantity).isEqualTo(3);
    }

    @Test
    @DisplayName("Ta bort produkt ska minska kvantiteten med 1")
    void removeProduct_shouldDecreaseQuantity() {
        Product apple = new Product("Apple", 10.0);
        cart.addProduct(apple, 5);

        cart.removeProduct(apple);

        assertThat(cart.getQuantity(apple)).isEqualTo(4);
        assertThat(cart.getItemCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ta bort produkt med angiven kvantitet ska minska korrekt antal")
    void removeProductWithQuantity_shouldRemoveCorrectAmount() {
        Product apple = new Product("Apple", 10.0);
        cart.addProduct(apple, 5);

        cart.removeProduct(apple, 3);

        assertThat(cart.getQuantity(apple)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ta bort alla av en produkt ska ta bort den från varukorgen")
    void removeAllOfProduct_shouldRemoveFromCart() {
        Product apple = new Product("Apple", 10.0);
        cart.addProduct(apple, 3);

        cart.removeProduct(apple, 3);

        assertThat(cart.containsProduct(apple)).isFalse();
        assertThat(cart.getQuantity(apple)).isEqualTo(0);
    }

    @Test
    @DisplayName("Försök att ta bort produkt som inte finns ska kasta exception")
    void removeProductThatDoesntExist_shouldThrowException() {
        Product apple = new Product("Apple", 10.0);

        assertThatThrownBy(() -> cart.removeProduct(apple))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}