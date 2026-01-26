package com.example.payment;

public interface PaymentRepository {
    void savePayment(double amount, PaymentStatus status);
}
