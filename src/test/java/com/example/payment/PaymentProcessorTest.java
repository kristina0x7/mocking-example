package com.example.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentApiClient paymentApiClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmailSender  emailSender;

    @InjectMocks
    private PaymentProcessor paymentProcessor;


    @Nested
    @DisplayName("Konstruktor-tester")
    class ConstructorTests {

        @Test
        @DisplayName("Null PaymentApiClient kastar NullPointerException")
        void constructor_NullPaymentApiClient_ThrowsNullPointerException() {
            assertThatThrownBy(() ->
                    new PaymentProcessor(null, paymentRepository, emailSender))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("paymentApiClient cannot be null");
        }
    }

    @Test
    @DisplayName("Null PaymentRepository kastar NullPointerException")
    void constructor_NullPaymentRepository_ThrowsNullPointerException() {
        assertThatThrownBy(() ->
                new PaymentProcessor(paymentApiClient, null, emailSender))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("paymentRepository cannot be null");
    }

    @Test
    @DisplayName("Null EmailSender kastar NullPointerException")
    void constructor_NullEmailSender_ThrowsNullPointerException() {
        assertThatThrownBy(() ->
                new PaymentProcessor(paymentApiClient, paymentRepository, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("emailSender cannot be null");
    }
}