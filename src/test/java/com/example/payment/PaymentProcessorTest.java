package com.example.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    private static final double VALID_AMOUNT = 100.50;
    private static final String VALID_TRANSACTION_ID = "id_0000000000";

    private void stubSuccessfulPayment() throws PaymentProcessingException {
        PaymentApiResponse response = new PaymentApiResponse(true, VALID_TRANSACTION_ID);
        when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(response);
    }

    private boolean pay() throws PaymentProcessingException {
        return paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);
    }

    private void verifyPaymentSaved() throws Exception {
        verify(paymentRepository).savePayment(amountCaptor.capture(), statusCaptor.capture(), transactionIdCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);
        assertThat(statusCaptor.getValue()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(transactionIdCaptor.getValue()).isEqualTo(VALID_TRANSACTION_ID);
    }

    private void verifyEmailSent() throws Exception {
        verify(emailSender).sendPaymentConfirmation(emailCaptor.capture(), amountCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo(VALID_EMAIL);
        assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);
    }

    @Nested
    class Constructor {

        @Test
        void withValidDependencies_Success() {
            PaymentProcessor processor = new PaymentProcessor(paymentApiClient, paymentRepository, emailSender);
            assertThat(processor).isNotNull();
        }

        @Test
        void nullPaymentApiClient_Throws() {
            assertThatThrownBy(() -> new PaymentProcessor(null, paymentRepository, emailSender))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("paymentApiClient cannot be null");
        }

        @Test
        void nullPaymentRepository_Throws() {
            assertThatThrownBy(() -> new PaymentProcessor(paymentApiClient, null, emailSender))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("paymentRepository cannot be null");
        }

        @Test
        void nullEmailSender_Throws() {
            assertThatThrownBy(() -> new PaymentProcessor(paymentApiClient, paymentRepository, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("emailSender cannot be null");
        }
    }


    @Nested
    class ProcessPaymentValidation {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -100.50})
        void nonPositiveAmount_Throws(double invalidAmount) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(invalidAmount, VALID_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be positive");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }

        @Test
        void nullEmail_Throws() {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        void emptyOrBlankEmail_Throws(String invalidEmail) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }
    }


    @Nested
    class ProcessPaymentSuccessful {

        @BeforeEach
        void setUp() throws PaymentProcessingException {
            stubSuccessfulPayment();
        }

        @Test
        void returnsTrue() throws PaymentProcessingException {
            assertThat(pay()).isTrue();
        }

        @Test
        void callsApiClient() throws PaymentProcessingException {
            pay();
            verify(paymentApiClient).charge(VALID_AMOUNT);
        }

        @Test
        void savesPayment() throws Exception {
            pay();
            verifyPaymentSaved();
        }

        @Test
        void sendsEmail() throws Exception {
            pay();
            verifyEmailSent();
        }
    }

    @Nested
    class ProcessPaymentFailed {

        @Test
        void apiFails_ReturnsFalse() throws Exception {
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(new PaymentApiResponse(false, null));
            boolean result = pay();
            assertThat(result).isFalse();
            verify(paymentRepository, never()).savePayment(anyDouble(), any(), anyString());
            verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
        }

        @Test
        void apiFailsWithTransactionId_NoSave() throws Exception {
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(new PaymentApiResponse(false, "failed-txn"));
            boolean result = pay();
            assertThat(result).isFalse();
            verify(paymentRepository, never()).savePayment(anyDouble(), any(), anyString());
            verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
        }


        @Nested
        class ProcessPaymentExceptions {

            @Test
            void repositoryThrows_paymentProcessingException() throws Exception {
                stubSuccessfulPayment();
                PaymentDataAccessException repoEx = new PaymentDataAccessException("DB error");
                doThrow(repoEx).when(paymentRepository).savePayment(VALID_AMOUNT, PaymentStatus.COMPLETED, VALID_TRANSACTION_ID);
                assertThatThrownBy(() -> pay())
                        .isInstanceOf(PaymentProcessingException.class)
                        .hasMessage("Failed to save payment")
                        .hasCause(repoEx);
                verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
            }

            @Test
            void emailThrows_paymentStillSuccessful() throws Exception {
                stubSuccessfulPayment();
                doThrow(new EmailSendingException("error")).when(emailSender).sendPaymentConfirmation(VALID_EMAIL, VALID_AMOUNT);
                assertThat(pay()).isTrue();
                verifyPaymentSaved();
            }

            @Test
            void apiThrows_bubblesUp() throws PaymentProcessingException {
                PaymentProcessingException apiEx = new PaymentProcessingException("API failed");
                when(paymentApiClient.charge(VALID_AMOUNT)).thenThrow(apiEx);
                assertThatThrownBy(() -> pay()).isInstanceOf(PaymentProcessingException.class).hasMessage("API failed");
                verifyNoInteractions(paymentRepository, emailSender);
            }
        }
    }
}