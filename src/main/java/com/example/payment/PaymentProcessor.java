package com.example.payment;

import java.util.Objects;

public class PaymentProcessor {

    private final PaymentApiClient paymentApiClient;
    private final PaymentRepository paymentRepository;
    private final EmailSender emailSender;

    public PaymentProcessor(PaymentApiClient paymentApiClient,
                            PaymentRepository paymentRepository,
                            EmailSender emailSender) {
        this.paymentApiClient = Objects.requireNonNull(paymentApiClient, "paymentApiClient cannot be null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "paymentRepository cannot be null");
        this.emailSender = Objects.requireNonNull(emailSender, "emailSender cannot be null");
    }

    public boolean processPayment(double amount, String email) throws PaymentProcessingException {
        if (amount <= 0) { throw new IllegalArgumentException("Amount must be positive");}
        if (email == null || email.isBlank()) { throw new IllegalArgumentException("Email cannot be null or empty");}

        PaymentApiResponse response = paymentApiClient.charge(amount);
        boolean success = response.isSuccess();

        if (success) {

            if (response.transactionId() == null || response.transactionId().isBlank()) {
                throw new PaymentProcessingException("Successful payment is missing transaction id");
            }

            try {
                paymentRepository.savePayment(amount, PaymentStatus.COMPLETED, response.transactionId());
            } catch (PaymentDataAccessException e) {
                // Wrap och kasta vidare
                throw new PaymentProcessingException("Failed to save payment");
            }

            try {
                emailSender.sendPaymentConfirmation(email, amount);
            } catch (EmailSendingException e) {
                // Fortsätt
            }
        }
        return success;
    }
}