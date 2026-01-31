package com.example.shop;

import java.util.Objects;

public class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        if (id.trim().isEmpty()) {throw new IllegalArgumentException("id cannot be empty or blank");}

        this.name = Objects.requireNonNull(name, "name cannot be null");
        if (name.trim().isEmpty()) {throw new IllegalArgumentException("name cannot be empty or blank");}

        if (price < 0) {throw new IllegalArgumentException("price cannot be negative: " + price);}
        this.price = price;
    }

    public String getId() {return id;}
    public String getName() {return name;}
    public double getPrice() {return price;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(price, product.price) == 0 && Objects.equals(id, product.id) && Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}