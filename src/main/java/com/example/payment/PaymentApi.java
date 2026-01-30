package com.example.payment;

public class PaymentApi {
    public static PaymentApiResponse charge(String apiKey, double amount) {
        return new PaymentApiResponse(true, "dummy");
    }
}
