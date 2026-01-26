package com.example.payment;

public interface EmailSender {
    void sendPaymentConfirmation(String email, double amount);
}
