package com.example.payment;

public record PaymentApiResponse(boolean isSuccess, String transactionId) {
}