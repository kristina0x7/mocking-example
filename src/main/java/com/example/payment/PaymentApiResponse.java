package com.example.payment;

public class PaymentApiResponse {
    private final boolean success;

    public PaymentApiResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
