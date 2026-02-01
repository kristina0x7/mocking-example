package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;


class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Nested
    @DisplayName("Tom varukorg")
    class EmptyCartTests {

        @Test
        @DisplayName("Ny varukorg ska vara tom")
        void newCart_ShouldBeEmpty() {
            assertThat(cart.isEmpty()).isTrue();
            assertThat(cart.getItemCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Ny varukorg ska ha totalpris 0")
        void newCart_shouldHaveZeroTotalPrice() {
            assertThat(cart.getTotalPrice()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Lägga till produkter")
    class AddProductTests {

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
    }

    @Nested
    @DisplayName("Hämta information")
    class InformationTests {

        @Test
        @DisplayName("Hämta kvantitet ska returnera korrekt antal")
        void getQuantity_shouldReturnCorrectAmount() {
            Product apple = new Product("Apple", 10.0);

            cart.addProduct(apple, 3);
            int quantity = cart.getQuantity(apple);

            assertThat(quantity).isEqualTo(3);
        }
    }


    @Nested
    @DisplayName("Ta bort produkter")
    class RemoveProductTests {

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
        void removeProductThatDoesntExist_shouldThrowCartException() {
            Product apple = new Product("Apple", 10.0);

            assertThatThrownBy(() -> cart.removeProduct(apple))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("not found");
        }
    }


    @Nested
    @DisplayName("Rabatter")
    class DiscountTests {

        @Test
        @DisplayName("Procentuell rabatt ska tillämpas på varukorgens totalpris")
        void getDiscountedPrice_withPercentageDiscount_shouldApplyDiscount() {
            Product apple = new Product("Apple", 100.0);
            cart.addProduct(apple);

            Discount discount = new PercentageDiscount(10.0);
            double discounted = cart.getDiscountedPrice(discount);

            assertThat(discounted).isEqualTo(90.0);
        }

        @Test
        @DisplayName("Fast rabatt ska dras av från varukorgens totalpris")
        void getDiscountedPrice_withFixedDiscount_shouldApplyDiscount() {
            Product banana = new Product("Banana", 50.0);
            cart.addProduct(banana, 3);

            Discount discount = new FixedDiscount(20.0);
            double discounted = cart.getDiscountedPrice(discount);

            assertThat(discounted).isEqualTo(130.0);
        }

        @Nested
        @DisplayName("Kvantitetsuppdateringar")
        class QuantityUpdateTests {

            @Test
            @DisplayName("Uppdatera kvantitet ska sätta nytt antal för produkt")
            void updateQuantity_shouldChangeQuantity() {
                Product apple = new Product("Apple", 10.0);
                cart.addProduct(apple, 2);

                cart.updateQuantity(apple, 5);

                assertThat(cart.getQuantity(apple)).isEqualTo(5);
                assertThat(cart.getItemCount()).isEqualTo(5);
            }

            @Test
            @DisplayName("Öka kvantitet ska lägga till angivet antal")
            void increaseQuantity_shouldAddAmount() {
                Product apple = new Product("Apple", 10.0);
                cart.addProduct(apple, 2);

                cart.increaseQuantity(apple, 3);

                assertThat(cart.getQuantity(apple)).isEqualTo(5);
            }

            @Test
            @DisplayName("Minska kvantitet ska dra bort angivet antal")
            void decreaseQuantity_shouldSubtractAmount() {
                Product apple = new Product("Apple", 10.0);
                cart.addProduct(apple, 5);

                cart.decreaseQuantity(apple, 2);

                assertThat(cart.getQuantity(apple)).isEqualTo(3);
            }
        }


        @Nested
        @DisplayName("Data access och immutability")
        class DataAccessTests {

            @Test
            @DisplayName("Hämta varukorgens innehåll ska returnera alla produkter med rätt kvantitet")
            void getItems_shouldReturnAllProducts() {
                Product apple = new Product("Apple", 10.0);
                Product banana = new Product("Banana", 15.0);

                cart.addProduct(apple, 2);
                cart.addProduct(banana, 3);

                Map<UUID, Integer> items = cart.getItems();

                assertThat(items).hasSize(2);
                assertThat(items.get(apple.getId())).isEqualTo(2);
                assertThat(items.get(banana.getId())).isEqualTo(3);
            }

            @Test
            @DisplayName("Ska returnera korrekt produkt-kvantitet mapping med immutable map")
            void getProductsWithQuantities_shouldReturnCorrectProductQuantityMapping() {
                Product apple = new Product("Apple", 10.0);
                Product banana = new Product("Banana", 15.0);

                cart.addProduct(apple, 2);
                cart.addProduct(banana, 3);

                Map<Product, Integer> items = cart.getProductsWithQuantities();

                assertThat(items)
                        .hasSize(2)
                        .containsEntry(apple, 2)
                        .containsEntry(banana, 3)
                        .isUnmodifiable();

                int totalItems = items.values().stream().mapToInt(Integer::intValue).sum();
                assertThat(totalItems).isEqualTo(5);
                assertThat(cart.getItemCount()).isEqualTo(5);
            }
        }
    }
}