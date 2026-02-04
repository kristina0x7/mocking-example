package com.example.shop;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    }

    @Nested
    class Equality {
        @Test
        void equals_andHashCode_basedOnId() {
            UUID id = UUID.randomUUID();
            Product p1 = new Product(id, "CPU1", 1000.0);
            Product p2 = new Product(id, "CPU2", 2000.0);
            Product p3 = new Product("CPU3", 1000.0);
            assertEquals(p1, p2);
            assertEquals(p1.hashCode(), p2.hashCode());
            assertNotEquals(p1, p3);
        }
    }

    @Nested
    class toString {
        @Test
        void toString_containsAllFields() {
            Product product = new Product("CPU", 3700.0);
            String str = product.toString();
            assertTrue(str.contains("id=" + product.getId()));
            assertTrue(str.contains("name='CPU'"));
            assertTrue(str.contains("price=3700.0"));
        }
    }
}