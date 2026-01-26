package com.example.payment;

public class PaymentProcessor {

    private final PaymentApiClient paymentApiClient;
    private final PaymentRepository paymentRepository;
    private final EmailSender emailSender;

    public PaymentProcessor(PaymentApiClient paymentApiClient,
                            PaymentRepository paymentRepository,
                            EmailSender emailSender) {
        this.paymentApiClient = paymentApiClient;
        this.paymentRepository = paymentRepository;
        this.emailSender = emailSender;
    }

    public boolean processPayment(double amount) {
        PaymentApiResponse response = paymentApiClient.charge(amount);

        if (response.success()) {
            paymentRepository.savePayment(amount, PaymentStatus.SUCCESS);
            emailSender.sendPaymentConfirmation("user@example.com", amount);
        }
        return response.success();
    }
}