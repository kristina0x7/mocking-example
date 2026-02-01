package com.example.shop;

public class FixedDiscount implements Discount {
    private final double amount;

    public FixedDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.amount = amount;
    }

    @Override
    public double apply(double originalPrice) {
        return Math.max(0, originalPrice - amount);
    }
}
