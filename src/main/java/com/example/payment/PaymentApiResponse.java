package com.example.payment;

public record PaymentApiResponse(boolean isSuccess, String transactionId) {

    public PaymentApiResponse {
        if (isSuccess && (transactionId == null || transactionId.isBlank())) {
            throw new IllegalArgumentException("Transaction ID is required for successful payments");}
    }

    public static PaymentApiResponse success(String transactionId) {
        return new PaymentApiResponse(true, transactionId);
    }

    public static PaymentApiResponse failure() {
        return new PaymentApiResponse(false, null);
    }
}