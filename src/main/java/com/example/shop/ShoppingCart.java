package com.example.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCart {
    private final Map<UUID, CartItem> items = new HashMap<>();

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addProduct(Product product, int quantity) {
        items.merge(product.getId(), new CartItem(product, quantity), (old, ne) -> {
            old.addQuantity(quantity);
            return old;
        });
    }

    public int getItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}