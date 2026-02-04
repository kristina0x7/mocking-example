package com.example.shop;

public class PercentageDiscount implements Discount {
    private final double percentage;

    public PercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {throw new IllegalArgumentException("Percentage must be between 0 and 100");}
        this.percentage = percentage;
    }

    @Override
    public double apply(double originalPrice) {
        return originalPrice * (1 - percentage / 100);
    }
}
