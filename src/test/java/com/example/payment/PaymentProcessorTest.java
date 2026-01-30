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
        @DisplayName("Skapa PaymentProcessor med null PaymentApiClient - ska kasta NullPointerException")
        void constructor_NullPaymentApiClient_ThrowsNullPointerException() {
            assertThatThrownBy(() ->
                    new PaymentProcessor(null, paymentRepository, emailSender))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("paymentApiClient cannot be null");
        }
    }

}