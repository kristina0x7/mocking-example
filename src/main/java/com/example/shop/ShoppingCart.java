package com.example.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShoppingCart {
    private final Map<UUID, CartItem> items = new HashMap<>();
    private Discount discount;

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

    public int getQuantity(UUID productId) {
        CartItem item = items.get(productId);
        return item != null ? item.getQuantity() : 0;
    }

    public void removeProduct(UUID productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) throw new CartException("Product not found in cart: " + productId);
        if (quantity >= item.getQuantity()) {
            items.remove(productId);
        } else {
            item.removeQuantity(quantity);
        }
    }

    public double getTotalPrice() {
        double total = items.values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        if (discount != null) total = discount.apply(total);
        return total;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public boolean containsProduct(UUID productId) {
        return items.containsKey(productId);
    }

    public void clear() {
        items.clear();
        discount = null;
    }
}