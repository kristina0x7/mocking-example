package com.example.payment;

import java.util.Objects;

public record PaymentApiResponse(boolean isSuccess, String transactionId) {

    public PaymentApiResponse {
        if (isSuccess) {
            Objects.requireNonNull(transactionId, "transactionId cannot be null for successful payment");
            if (transactionId.isBlank()) {
                throw new IllegalArgumentException("transactionId cannot be blank for successful payment");
            }
        }
    }
}