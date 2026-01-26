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

    public boolean processPayment(double amount) {
        PaymentApiResponse response = paymentApiClient.charge(amount);

        if (response.isSuccess()) {

            try {
                paymentRepository.savePayment(amount, PaymentStatus.SUCCESS);
            } catch (PaymentPersistenceException e) {
                // Fortsätt
            }

            try {
                emailSender.sendPaymentConfirmation("user@example.com", amount);
            } catch (EmailSendingException e) {
                // Fortsätt
            }
        }
        return response.isSuccess();
    }
}