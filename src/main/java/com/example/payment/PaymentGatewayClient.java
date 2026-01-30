package com.example.payment;

public class PaymentGatewayClient implements PaymentApiClient {
    private final String apiKey;

    public PaymentGatewayClient(String apiKey) {
        this.apiKey = apiKey;
    }
}