package com.example.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private EmailSender emailSender;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<Double> amountCaptor;

    @Captor
    private ArgumentCaptor<PaymentStatus> statusCaptor;

    @Captor
    private ArgumentCaptor<String> transactionIdCaptor;

    private static final String VALID_EMAIL = "name@example.com";


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

    @Nested
    @DisplayName("Validering av input")
    class InputValidationTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -100.50})
        @DisplayName("Amount <= 0 kastar IllegalArgumentException")
        void processPayment_NonPositiveAmount_ThrowsIllegalArgumentException(double invalidAmount) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(invalidAmount, VALID_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be positive");
        }
    }

    @Nested
    @DisplayName("Lyckad betalning")
    class HappyPathTests {

        @Test
        void paymentSavedAndEmailSent() {
        }
    }

    @Nested
    @DisplayName("Misslyckad betalning")
    class FailedPaymentTests {

        @Test
        void failedPayment_noSaveNoEmail() {
        }
    }

    @Nested
    @DisplayName("Lyckad betalning utan transactionId")
    class MissingTransactionIdTests {

        @Test
        void nullTransactionId_throwsPaymentProcessingException() {
        }

        @Test
        void blankTransactionId_throwsPaymentProcessingException() {
        }
    }

    @Nested
    @DisplayName("Repository-fel")
    class RepositoryExceptionTests {

        @Test
        void savePaymentFails_throwsPaymentProcessingException() {
        }
    }

    @Nested
    @DisplayName("Email-fel")
    class EmailExceptionTests {

        @Test
        void emailFailure_doesNotFailPayment() {

        }
    }
}