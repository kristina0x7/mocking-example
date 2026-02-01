package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;


class ShoppingCartTest {
    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        apple = new Product("Apple", 10.0);
        banana = new Product("Banana", 15.0);
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

        @Test
        @DisplayName("Ska returnera tom Map från getItems")
        void newCart_getItems_shouldReturnEmptyMap() {
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Ska returnera tom Map från getProductsWithQuantities")
        void newCart_getProductsWithQuantities_shouldReturnEmptyMap() {
            assertThat(cart.getProductsWithQuantities()).isEmpty();
        }
    }


    @Nested
    @DisplayName("Lägga till produkter")
    class AddProductTests {

        @Test
        @DisplayName("Lägg till produkt ska öka antalet i varukorgen")
        void addProduct_shouldIncreaseItemCount() {
            cart.addProduct(apple);

            assertThat(cart.getItemCount()).isEqualTo(1);
            assertThat(cart.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("Lägg till produkt med kvantitet ska lägga till rätt antal")
        void addProductWithQuantity_shouldAddCorrectAmount() {
            cart.addProduct(apple, 3);

            assertThat(cart.getItemCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Lägga till samma produkt flera gånger ska summera kvantiteter")
        void addSameProductTwice_shouldSumQuantities() {
            cart.addProduct(apple, 2);
            cart.addProduct(apple, 3);

            assertThat(cart.getItemCount()).isEqualTo(5);
        }

        @ParameterizedTest
        @ValueSource(ints = {-5, -1, 0})
        @DisplayName("Lägga till produkt med ogiltig kvantitet ska kasta exception")
        void addProduct_withInvalidQuantity_shouldThrowException(int invalidQuantity) {
            assertThatThrownBy(() -> cart.addProduct(apple, invalidQuantity))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Lägga till null produkt ska kasta exception")
        void addNullProduct_shouldThrowException() {
            assertThatThrownBy(() -> cart.addProduct(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Product cannot be null");
        }
    }


    @Nested
    @DisplayName("Hämta information")
    class InformationTests {

        @BeforeEach
        void setUpWithProducts() {
            cart.addProduct(apple, 3);
        }

        @Test
        @DisplayName("Hämta kvantitet ska returnera korrekt antal")
        void getQuantity_shouldReturnCorrectAmount() {
            int quantity = cart.getQuantity(apple);
            assertThat(quantity).isEqualTo(3);
        }

        @Test
        @DisplayName("Hämta kvantitet för produkt som inte finns ska returnera 0")
        void getQuantity_nonExistentProduct_shouldReturnZero() {
            assertThat(cart.getQuantity(banana)).isEqualTo(0);
        }

        @Test
        @DisplayName("containsProduct ska returnera true för produkt i varukorgen")
        void containsProduct_shouldReturnTrueForProductInCart() {
            assertThat(cart.containsProduct(apple)).isTrue();
        }

        @Test
        @DisplayName("containsProduct ska returnera false för produkt inte i varukorgen")
        void containsProduct_shouldReturnFalseForProductNotInCart() {
            assertThat(cart.containsProduct(banana)).isFalse();
        }

        @Test
        @DisplayName("getProductById ska returnera produkt om den finns")
        void getProductById_shouldReturnProductIfExists() {
            assertThat(cart.getProductById(apple.getId())).contains(apple);
        }

        @Test
        @DisplayName("getProductById ska returnera empty för produkt som inte finns")
        void getProductById_shouldReturnEmptyForNonExistentProduct() {
            assertThat(cart.getProductById(UUID.randomUUID())).isEmpty();
        }
    }


    @Nested
    @DisplayName("Ta bort produkter")
    class RemoveProductTests {

        @BeforeEach
        void setUpWithProducts() {
            cart.addProduct(apple, 5);
            cart.addProduct(banana, 3);
        }

        @Test
        @DisplayName("Ta bort produkt ska minska kvantiteten med 1")
        void removeProduct_shouldDecreaseQuantity() {
            cart.removeProduct(apple);

            assertThat(cart.getQuantity(apple)).isEqualTo(4);
            assertThat(cart.getItemCount()).isEqualTo(7);
        }

        @Test
        @DisplayName("Ta bort produkt med angiven kvantitet ska minska korrekt antal")
        void removeProductWithQuantity_shouldRemoveCorrectAmount() {
            cart.removeProduct(apple, 3);

            assertThat(cart.getQuantity(apple)).isEqualTo(2);
        }

        @Test
        @DisplayName("Ta bort alla av en produkt ska ta bort den från varukorgen")
        void removeAllOfProduct_shouldRemoveFromCart() {
            cart.removeProduct(apple, 3);
            cart.removeProduct(apple, 2);

            assertThat(cart.containsProduct(apple)).isFalse();
            assertThat(cart.getQuantity(apple)).isEqualTo(0);
            assertThat(cart.getItemCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Försök att ta bort produkt som inte finns ska kasta exception")
        void removeProductThatDoesntExist_shouldThrowCartException() {
            Product orange = new Product("Orange", 20.0);

            assertThatThrownBy(() -> cart.removeProduct(orange))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("not found");
        }

        @ParameterizedTest
        @ValueSource(ints = {-5, -1, 0})
        @DisplayName("Ta bort med ogiltig kvantitet ska kasta exception")
        void removeWithInvalidQuantity_shouldThrowException(int invalidQuantity) {
            assertThatThrownBy(() -> cart.removeProduct(apple, invalidQuantity))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Ta bort fler än finns ska kasta exception")
        void removeMoreThanExists_shouldThrowException() {
            assertThatThrownBy(() -> cart.removeProduct(apple, 10))
                    .isInstanceOf(CartException.class)
                    .hasMessageContaining("only 5 in cart");
        }
    }


    @Nested
    @DisplayName("Rabatter")
    class DiscountTests {

        @Test
        @DisplayName("Procentuell rabatt ska tillämpas på varukorgens totalpris")
        void getDiscountedPrice_withPercentageDiscount_shouldApplyDiscount() {
            cart.addProduct(apple, 2);

            Discount discount = new PercentageDiscount(10.0);
            double discounted = cart.getDiscountedPrice(discount);

            assertThat(discounted).isEqualTo(18.0);
        }

        @Test
        @DisplayName("Fast rabatt ska dras av från varukorgens totalpris")
        void getDiscountedPrice_withFixedDiscount_shouldApplyDiscount() {
            cart.addProduct(banana, 3);

            Discount discount = new FixedDiscount(20.0);
            double discounted = cart.getDiscountedPrice(discount);

            assertThat(discounted).isEqualTo(25.0);
        }
    }


        @Nested
        @DisplayName("Kvantitetsuppdateringar")
        class QuantityUpdateTests {

            @BeforeEach
            void setUpWithProduct() {
                cart.addProduct(apple, 2);
            }

            @Test
            @DisplayName("Uppdatera kvantitet ska sätta nytt antal för produkt")
            void updateQuantity_shouldChangeQuantity() {
                cart.updateQuantity(apple, 5);

                assertThat(cart.getQuantity(apple)).isEqualTo(5);
                assertThat(cart.getItemCount()).isEqualTo(5);
            }

            @Test
            @DisplayName("Öka kvantitet ska lägga till angivet antal")
            void increaseQuantity_shouldAddAmount() {
                cart.increaseQuantity(apple, 3);

                assertThat(cart.getQuantity(apple)).isEqualTo(5);
            }

            @Test
            @DisplayName("Minska kvantitet ska dra bort angivet antal")
            void decreaseQuantity_shouldSubtractAmount() {
                cart.decreaseQuantity(apple, 1);

                assertThat(cart.getQuantity(apple)).isEqualTo(1);
            }

            @Test
            @DisplayName("Minska kvantitet till 0 ska ta bort produkten")
            void decreaseQuantity_toZero_shouldRemoveProduct() {
                cart.decreaseQuantity(apple, 2);

                assertThat(cart.containsProduct(apple)).isFalse();
                assertThat(cart.isEmpty()).isTrue();
            }

            @ParameterizedTest
            @ValueSource(ints = {-5, -1, 0})
            @DisplayName("Uppdatera till ogiltig kvantitet ska kasta exception")
            void updateToInvalidQuantity_shouldThrowException(int invalidQuantity) {
                assertThatThrownBy(() -> cart.updateQuantity(apple, invalidQuantity))
                        .isInstanceOf(CartException.class)
                        .hasMessageContaining("Quantity must be positive");
            }

            @Test
            @DisplayName("Uppdatera kvantitet för produkt som inte finns ska kasta exception")
            void updateQuantity_nonExistentProduct_shouldThrowException() {
                assertThatThrownBy(() -> cart.updateQuantity(banana, 5))
                        .isInstanceOf(CartException.class)
                        .hasMessageContaining("not found");
            }
        }


        @Nested
        @DisplayName("Data access och immutability")
        class DataAccessTests {

            @Test
            @DisplayName("Hämta varukorgens innehåll ska returnera alla produkter med rätt kvantitet")
            void getItems_shouldReturnAllProducts() {
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