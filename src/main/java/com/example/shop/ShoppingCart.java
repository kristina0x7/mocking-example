package com.example.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
}