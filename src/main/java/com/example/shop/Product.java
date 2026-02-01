package com.example.shop;

import java.util.Objects;
import java.util.UUID;

public class Product {
    private final UUID id;
    private final String name;
    private final double price;

    public Product(UUID id, String name, double price) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty or blank");
        }

        if (price < 0) {
            throw new IllegalArgumentException("price cannot be negative: " + price);
        }
        this.price = price;
    }

    public Product(String name, double price) {
        this(UUID.randomUUID(), name, price);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}