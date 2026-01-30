package com.example.payment;

public class PaymentGatewayClient implements PaymentApiClient {
    private final String apiKey;

    public PaymentGatewayClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        this.apiKey = apiKey;
    }

    @Override
    public PaymentApiResponse charge(double amount) {

        PaymentApiResponse externalResponse = PaymentApi.charge(apiKey, amount);

        return new PaymentApiResponse(
                externalResponse.isSuccess(),
                externalResponse.getTransactionId()

        );
    }
}