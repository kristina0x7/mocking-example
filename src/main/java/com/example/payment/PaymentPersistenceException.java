package com.example.payment;

public class PaymentPersistenceException extends RuntimeException {
    public PaymentPersistenceException(String message, PaymentPersistenceException e) {
        super(message);
    }
}
