package com.example.shop;

import java.util.*;

public class ShoppingCart {
    private final Map<UUID, Integer> quantities;
    private final Map<UUID, Product> products;

    public ShoppingCart() {
        this.quantities = new HashMap<>();
        this.products = new HashMap<>();
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public int getItemCount() {
        return quantities.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public double getTotalPrice() {
        return quantities.entrySet().stream()
                .mapToDouble(entry -> {
                    UUID productId = entry.getKey();
                    Product product = products.get(productId);
                    int quantity = entry.getValue();
                    return product.getPrice() * quantity;
                })
                .sum();
    }

    public void addProduct(Product product) {
        addProduct(product, 1);
    }

    public void addProduct(Product product, int quantity) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        UUID productId = product.getId();
        products.putIfAbsent(productId, product);
        quantities.merge(productId, quantity, Integer::sum);
    }

    public int getQuantity(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        return quantities.getOrDefault(product.getId(), 0);
    }

    public boolean containsProduct(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        return quantities.containsKey(product.getId());
    }

    public Optional<Product> getProductById(UUID productId) {
        return Optional.ofNullable(products.get(productId));
    }
}