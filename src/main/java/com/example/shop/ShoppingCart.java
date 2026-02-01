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
                    Product product = products.get(entry.getKey());
                    return product != null ? product.getPrice() * entry.getValue() : 0.0;
                })
                .sum();
    }

    public void addProduct(Product product) {
        addProduct(product, 1);
    }

    public void addProduct(Product product, int quantity) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (quantity <= 0) {
            throw new CartException("Quantity must be positive");
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

    public void removeProduct(Product product) {
        removeProduct(product, 1);
    }

    public void removeProduct(Product product, int quantity) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (quantity <= 0) {
            throw new CartException("Quantity must be positive");
        }

        UUID productId = product.getId();
        Integer current = quantities.get(productId);

        if (current == null) {
            throw new CartException("Product not found in cart: " + product.getName());
        }

        if (current < quantity) {
            throw new CartException(
                    "Cannot remove " + quantity + " of " + product.getName() + ", only " + current + " in cart"
            );
        }
        if (current == quantity) {
            quantities.remove(productId);
            products.remove(productId);
        } else {
            quantities.put(productId, current - quantity);
        }
    }

    public double getDiscountedPrice(Discount discount) {
        double total = getTotalPrice();
        if (discount == null) {
            return total;
        }
        return discount.apply(total);
    }

    public void updateQuantity(Product product, int newQuantity) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (newQuantity <= 0) {
            throw new CartException("Quantity must be positive");
        }

        UUID productId = product.getId();
        if (!quantities.containsKey(productId)) {
            throw new CartException("Product not found in cart: " + product.getName());
        }
        quantities.put(productId, newQuantity);
    }

    public void increaseQuantity(Product product, int amount) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (amount <= 0) {
            throw new CartException("Increase amount must be positive");
        }

        UUID productId = product.getId();
        if (!quantities.containsKey(productId)) {
            throw new CartException("Product not found in cart: " + product.getName());
        }
        quantities.put(productId, quantities.get(productId) + amount);
    }

    public void decreaseQuantity(Product product, int amount) {
        Objects.requireNonNull(product, "Product cannot be null");

        if (amount <= 0) {
            throw new CartException("Decrease amount must be positive");
        }

        UUID productId = product.getId();
        Integer current = quantities.get(productId);

        if (current == null) {
            throw new CartException("Product not found in cart: " + product.getName());
        }

        if (amount > current) {
            throw new CartException(
                    "Cannot decrease by " + amount + ", only " + current + " items of " + product.getName() + " in cart"
            );
        }

        if (amount == current) {
            quantities.remove(productId);
            products.remove(productId);
        } else {
            quantities.put(productId, current - amount);
        }
    }

    public Map<UUID, Integer> getItems() {
        return new HashMap<>(quantities);
    }

    public Map<Product, Integer> getProductsWithQuantities() {
        Map<Product, Integer> result = new HashMap<>();
        for (Map.Entry<UUID, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product != null) {
                result.put(product, entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    public void clear() {
        quantities.clear();
        products.clear();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "ShoppingCart[empty]";
        }
        return String.format("Cart with %d items (%.2f kr)", getItemCount(), getTotalPrice());
    }
}