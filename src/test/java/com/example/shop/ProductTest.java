package com.example.shop;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Nested
    class Constructor {

        @Test
        void constructor_nullId_throws() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Product(null, "CPU", 3700.0)
            );
            assertEquals("id cannot be null", exception.getMessage());
        }

        @Test
        void constructor_nullName_throws() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> new Product(UUID.randomUUID(), null, 3700.0)
            );
            assertEquals("name cannot be null", exception.getMessage());
        }

        @Test
        void constructor_emptyName_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Product("", 3700.0)
            );
            assertEquals("name cannot be empty or blank", exception.getMessage());
        }

        @Test
        void constructor_blankName_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Product("   ", 3700.0)
            );
            assertEquals("name cannot be empty or blank", exception.getMessage());
        }

        @Test
        void constructor_negativePrice_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Product("CPU", -100.0)
            );
            assertEquals("price cannot be negative: -100.0", exception.getMessage());
        }

        @Test
        void constructor_uuidAndGetters_workCorrectly() {
            UUID id = UUID.randomUUID();
            Product product = new Product(id, "CPU", 3700.0);
            assertEquals(id, product.getId());
            assertEquals("CPU", product.getName());
            assertEquals(3700.0, product.getPrice(), 0.001);
        }

        @Test
        void constructor_nameAndPrice_generatesUUID() {
            Product product = new Product("CPU", 3700.0);
            assertNotNull(product.getId());
            assertEquals("CPU", product.getName());
            assertEquals(3700.0, product.getPrice(), 0.001);
        }

        @Test
        void constructor_zeroPrice_allows() {
            Product product = new Product("CPU", 0.0);
            assertEquals(0.0, product.getPrice(), 0.001);
        }
    }

    @Nested
    class Equality {

        @Test
        void equals_sameReference_isTrue() {
            Product product = new Product("CPU", 3700.0);
            assertTrue(product.equals(product));
        }

        @Test
        void equals_nullObject_isFalse() {
            Product product = new Product("CPU", 3700.0);
            assertFalse(product.equals(null));
        }

        @Test
        void equals_differentClass_isFalse() {
            Product product = new Product("CPU", 3700.0);
            Object other = "Not a Product";
            assertFalse(product.equals(other));
        }

        @Test
        void equals_sameId_isTrue() {
            UUID id = UUID.randomUUID();
            Product product1 = new Product(id, "CPU", 3700.0);
            Product product2 = new Product(id, "Different Name", 1000.0);
            assertTrue(product1.equals(product2));
            assertTrue(product2.equals(product1));
        }

        @Test
        void equals_differentId_isFalse() {
            Product product1 = new Product("CPU", 3700.0);
            Product product2 = new Product("CPU", 3700.0);
            assertFalse(product1.equals(product2));
            assertFalse(product2.equals(product1));
        }

        @Test
        void equals_symmetricProperty() {
            UUID id = UUID.randomUUID();
            Product product1 = new Product(id, "CPU", 3700.0);
            Product product2 = new Product(id, "GPU", 5000.0);
            assertTrue(product1.equals(product2));
            assertTrue(product2.equals(product1));
        }

        @Test
        void equals_transitiveProperty() {
            UUID id = UUID.randomUUID();
            Product product1 = new Product(id, "CPU", 3700.0);
            Product product2 = new Product(id, "CPU", 3700.0);
            Product product3 = new Product(id, "CPU", 3700.0);
            assertTrue(product1.equals(product2));
            assertTrue(product2.equals(product3));
            assertTrue(product1.equals(product3));
        }
    }

    @Nested
    class HashCode {

        @Test
        void hashCode_sameId_returnsSameHash() {
            UUID id = UUID.randomUUID();
            Product product1 = new Product(id, "CPU", 3700.0);
            Product product2 = new Product(id, "GPU", 5000.0);
            assertEquals(product1.hashCode(), product2.hashCode());
        }

        @Test
        void hashCode_differentId_returnsDifferentHash() {
            Product product1 = new Product("CPU", 3700.0);
            Product product2 = new Product("GPU", 5000.0);
            assertNotEquals(product1.hashCode(), product2.hashCode());
        }

        @Test
        void hashCode_consistency() {
            Product product = new Product("CPU", 3700.0);
            int expectedHashCode = Objects.hash(product.getId());
            assertEquals(expectedHashCode, product.hashCode());
            assertEquals(product.hashCode(), product.hashCode());
        }

        @Test
        void hashCode_equalsObjects_haveSameHashCode() {
            UUID id = UUID.randomUUID();
            Product product1 = new Product(id, "CPU", 3700.0);
            Product product2 = new Product(id, "CPU", 3700.0);
            assertTrue(product1.equals(product2));
            assertEquals(product1.hashCode(), product2.hashCode());
        }
    }

    @Nested
    class ToString {

        @Test
        void toString_containsAllFields() {
            UUID id = UUID.randomUUID();
            Product product = new Product(id, "CPU", 3700.0);
            String result = product.toString();
            assertTrue(result.contains(id.toString()));
            assertTrue(result.contains("CPU"));
            assertTrue(result.contains("3700.0"));
            assertTrue(result.contains("Product{"));
            assertTrue(result.contains("id="));
            assertTrue(result.contains("name="));
            assertTrue(result.contains("price="));
        }

        @Test
        void toString_zeroPrice_formattedCorrectly() {
            UUID id = UUID.randomUUID();
            Product product = new Product(id, "Free Item", 0.0);
            String result = product.toString();
            assertTrue(result.contains("price=0.0"));
        }
    }
}