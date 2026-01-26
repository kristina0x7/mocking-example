package com.example.payment;

public interface PaymentApiClient {
    PaymentApiResponse charge(double amount);
}
