package com.example.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCart {
    private final Map<UUID, CartItem> items = new HashMap<>();

    public boolean isEmpty() {
        return items.isEmpty();
    }
}