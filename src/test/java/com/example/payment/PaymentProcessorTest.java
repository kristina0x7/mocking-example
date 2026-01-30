package com.example.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private static final String VALID_TRANSACTION_ID = "id_111222333";


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

        @Test
        @DisplayName("Null email kastar IllegalArgumentException")
        void processPayment_NullEmail_ThrowsIllegalArgumentException() {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Tom eller blank email kastar IllegalArgumentException")
        void processPayment_EmptyOrBlankEmail_ThrowsIllegalArgumentException(String invalidEmail) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("Lyckad betalning")
    class HappyPathTests {

        private PaymentApiResponse successResponse;

        @BeforeEach
        void setUp() {

            successResponse = new PaymentApiResponse(true, VALID_TRANSACTION_ID);

            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(successResponse);
        }

        @Test
        @DisplayName("Lyckad betalning - allt sparas och email skickas")
        void processPayment_SuccessfulPayment_SavesPaymentAndSendsEmail() throws PaymentProcessingException,
                PaymentDataAccessException, EmailSendingException {

            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(result).isTrue();

            verify(paymentApiClient, times(1)).charge(VALID_AMOUNT);

            verify(paymentRepository).savePayment(
                    amountCaptor.capture(),
                    statusCaptor.capture(),
                    transactionIdCaptor.capture()
            );

            assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);
            assertThat(statusCaptor.getValue()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(transactionIdCaptor.getValue()).isEqualTo(VALID_TRANSACTION_ID);

            verify(emailSender).sendPaymentConfirmation(
                    emailCaptor.capture(),
                    amountCaptor.capture()
            );

            assertThat(emailCaptor.getValue()).isEqualTo(VALID_EMAIL);
            assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);

            verifyNoMoreInteractions(paymentApiClient, paymentRepository, emailSender);
        }

        @Test
        @DisplayName("Lyckad betalning returnerar true")
        void processPayment_SuccessfulPayment_ReturnsTrue() throws PaymentProcessingException {

            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Returnerar API:ets success-flagga")
        void processPayment_ReturnsApiSuccessFlag() throws PaymentProcessingException {

            boolean expectedSuccessValue = true;
            PaymentApiResponse apiResponse = new PaymentApiResponse(expectedSuccessValue, VALID_TRANSACTION_ID);
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(apiResponse);

            boolean actualResult = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(actualResult).isEqualTo(expectedSuccessValue);
            assertThat(actualResult).isEqualTo(apiResponse.isSuccess());
        }

    }
        @Nested
        @DisplayName("Misslyckad betalning")
        class FailedPaymentTests {

            @BeforeEach
            void setUp() {
                PaymentApiResponse failedResponse = new PaymentApiResponse(false, null);
                when(paymentApiClient.charge(anyDouble())).thenReturn(failedResponse);
            }

            @Test
            @DisplayName("Misslyckad betalning - inget sparas och inget email skickas")
            void processPayment_FailedPayment_NoSaveNoEmail() throws PaymentProcessingException,
                    PaymentDataAccessException, EmailSendingException {

                boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

                assertThat(result).isFalse();
                verify(paymentRepository, never()).savePayment(anyDouble(), any(), anyString());
                verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
                verify(paymentApiClient).charge(VALID_AMOUNT);
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
