package com.example.shop;

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void addQuantity(int amount) { quantity += amount; }
    public void removeQuantity(int amount) { quantity -= amount; }
    public boolean isEmpty() { return quantity <= 0; }

    public double getTotalPrice() {return product.getPrice() * quantity;}
}