package com.example.shop;

import java.util.Objects;

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "Product cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("CartItem quantity must be positive");
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void addQuantity(int amount) {if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");quantity += amount;}
    public void removeQuantity(int amount) {if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");quantity = Math.max(0, quantity - amount);}
    public boolean isEmpty() { return quantity <= 0; }
    public double getTotalPrice() {return product.getPrice() * quantity;}
}