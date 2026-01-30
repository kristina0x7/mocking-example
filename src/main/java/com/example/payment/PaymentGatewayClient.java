package com.example.payment;

public class PaymentGatewayClient implements PaymentApiClient {
    private final String apiKey;

        public PaymentGatewayClient(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("API key cannot be null or empty");
            }
            if (!apiKey.startsWith("sk_")) {
                throw new IllegalArgumentException("API key must start with 'sk_'");
            }
            this.apiKey = apiKey;
        }

        @Override
        public PaymentApiResponse charge(double amount) throws PaymentProcessingException {
            try {
                return PaymentApi.charge(apiKey, amount);
            } catch (Exception e) {
                throw new PaymentProcessingException("Payment API call failed", e);
            }
        }
    }