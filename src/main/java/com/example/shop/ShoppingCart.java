package com.example.shop;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<Product, Integer> items;

    public ShoppingCart() {
        this.items = new HashMap<>();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        return items.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public double getTotalPrice() {
        return 0.0;
    }
}