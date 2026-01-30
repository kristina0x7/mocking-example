package com.example.payment;

public class PaymentApi {
    private PaymentApi() {}

    public static PaymentApiResponse charge(String apiKey, double amount) {
        if (apiKey == null || apiKey.isBlank() || !apiKey.startsWith("sk_") || amount <= 0) {
            return PaymentApiResponse.failure();
        }

        return PaymentApiResponse.success("test-txn-123");
    }
}