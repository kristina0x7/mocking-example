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
        @DisplayName("Skapar PaymentProcessor med alla beroenden")
        void constructor_WithValidDependencies_Success() {
            PaymentProcessor processor = new PaymentProcessor(paymentApiClient, paymentRepository, emailSender);
            assertThat(processor).isNotNull();
        }

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
    @DisplayName("Input validering")
    class InputValidationTests {

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -100.50})
        @DisplayName("Amount <= 0 kastar IllegalArgumentException")
        void processPayment_NonPositiveAmount_ThrowsIllegalArgumentException(double invalidAmount) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(invalidAmount, VALID_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be positive");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }

        @Test
        @DisplayName("Null email kastar IllegalArgumentException")
        void processPayment_NullEmail_ThrowsIllegalArgumentException() {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Tom eller blank email kastar IllegalArgumentException")
        void processPayment_EmptyOrBlankEmail_ThrowsIllegalArgumentException(String invalidEmail) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null or empty");
            verifyNoInteractions(paymentApiClient, paymentRepository, emailSender);
        }
    }

    @Nested
    @DisplayName("Lyckad betalning")
    class SuccessfulPaymentTests {

        private PaymentApiResponse successResponse;

        @BeforeEach
        void setUp() throws PaymentProcessingException {
            successResponse = new PaymentApiResponse(true, VALID_TRANSACTION_ID);
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(successResponse);
        }

        @Test
        @DisplayName("Lyckad betalning returnerar true")
        void processPayment_SuccessfulPayment_ReturnsTrue() throws PaymentProcessingException {
            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Anropar PaymentApiClient.charge med korrekt belopp")
        void processPayment_CallsApiClientWithCorrectAmount() throws PaymentProcessingException {
            paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);
            verify(paymentApiClient).charge(VALID_AMOUNT);
        }

        @Test
        @DisplayName("Sparar betalning med korrekt data")
        void processPayment_SavesPaymentToRepository() throws PaymentProcessingException, PaymentDataAccessException {
            paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            verify(paymentRepository).savePayment(
                    amountCaptor.capture(),
                    statusCaptor.capture(),
                    transactionIdCaptor.capture()
            );

            assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);
            assertThat(statusCaptor.getValue()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(transactionIdCaptor.getValue()).isEqualTo(VALID_TRANSACTION_ID);
        }

        @Test
        @DisplayName("Skickar bekräftelse-email")
        void processPayment_SendsConfirmationEmail() throws PaymentProcessingException, EmailSendingException {
            paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            verify(emailSender).sendPaymentConfirmation(
                    emailCaptor.capture(),
                    amountCaptor.capture()
            );

            assertThat(emailCaptor.getValue()).isEqualTo(VALID_EMAIL);
            assertThat(amountCaptor.getValue()).isEqualTo(VALID_AMOUNT);
        }
    }

    @Nested
    @DisplayName("Misslyckad betalning (API returnerar false)")
    class FailedPaymentTests {

        @Test
        @DisplayName("API misslyckas - returnerar false och sparar inte")
        void processPayment_ApiFails_ReturnsFalseAndNoSave() throws PaymentProcessingException, PaymentDataAccessException, EmailSendingException {
            PaymentApiResponse failedResponse = new PaymentApiResponse(false, null);
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(failedResponse);

            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(result).isFalse();
            verify(paymentRepository, never()).savePayment(anyDouble(), any(), anyString());
            verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
        }

        @Test
        @DisplayName("API misslyckas med transactionId - sparar inte heller")
        void processPayment_ApiFailsWithTransactionId_NoSave() throws PaymentProcessingException, PaymentDataAccessException, EmailSendingException {

            PaymentApiResponse failedResponse = new PaymentApiResponse(false, "failed-txn-123");
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(failedResponse);

            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(result).isFalse();
            verify(paymentRepository, never()).savePayment(anyDouble(), any(), anyString());
            verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
        }
    }

    @Nested
    @DisplayName("Exception-hantering")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Repository-exception kastar PaymentProcessingException")
        void processPayment_RepositoryException_ThrowsPaymentProcessingException()
                throws PaymentDataAccessException, PaymentProcessingException, EmailSendingException {

            PaymentApiResponse successResponse = new PaymentApiResponse(true, VALID_TRANSACTION_ID);
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(successResponse);

            PaymentDataAccessException repoException = new PaymentDataAccessException("DB error");
            doThrow(repoException)
                    .when(paymentRepository)
                    .savePayment(VALID_AMOUNT, PaymentStatus.COMPLETED, VALID_TRANSACTION_ID);


            assertThatThrownBy(() -> paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage("Failed to save payment")
                    .hasCause(repoException);

            verify(emailSender, never()).sendPaymentConfirmation(anyString(), anyDouble());
        }

        @Test
        @DisplayName("Email-exception påverkar inte betalningen")
        void processPayment_EmailException_PaymentStillSuccessful()
                throws PaymentProcessingException, EmailSendingException, PaymentDataAccessException {

            PaymentApiResponse successResponse = new PaymentApiResponse(true, VALID_TRANSACTION_ID);
            when(paymentApiClient.charge(VALID_AMOUNT)).thenReturn(successResponse);

            doThrow(new EmailSendingException("SMTP error"))
                    .when(emailSender)
                    .sendPaymentConfirmation(VALID_EMAIL, VALID_AMOUNT);

            boolean result = paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL);

            assertThat(result).isTrue();
            verify(paymentRepository).savePayment(VALID_AMOUNT, PaymentStatus.COMPLETED, VALID_TRANSACTION_ID);
        }

        @Test
        @DisplayName("Payment API-exception kastas vidare")
        void processPayment_ApiThrowsPaymentProcessingException_BubblesUp() throws PaymentProcessingException {

            PaymentProcessingException apiException = new PaymentProcessingException("API failed");
            when(paymentApiClient.charge(VALID_AMOUNT)).thenThrow(apiException);

            assertThatThrownBy(() ->
                    paymentProcessor.processPayment(VALID_AMOUNT, VALID_EMAIL))
                    .isSameAs(apiException);

            verifyNoInteractions(paymentRepository, emailSender);
        }
    }
}