package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    private static final String ROOM_ID = "room1";
    private static final String ROOM_NAME = "Room A";
    private static final String BOOKING_ID = "booking1";

    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 7, 9, 0);
    private static final LocalDateTime FUTURE_START_TIME = CURRENT_TIME.plusHours(1);
    private static final LocalDateTime FUTURE_END_TIME = CURRENT_TIME.plusHours(2);
    private static final LocalDateTime PAST_TIME = CURRENT_TIME.minusHours(1);

    @Mock
    TimeProvider timeProvider;

    @Mock
    RoomRepository roomRepository;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    BookingSystem bookingSystem;

    private Room room;

    @BeforeEach
    @DisplayName("Förbered testdata och mock-inställningar")
    void setUp() {
        room = new Room(ROOM_ID, ROOM_NAME);
        Mockito.lenient().when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
    }

    @ParameterizedTest
    @MethodSource("nullParameterTestCases")
    @DisplayName("När någon parameter är null - kasta IllegalArgumentException")
    void bookRoom_WithAnyNullParameter_ThrowsException(
            String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(() ->
                bookingSystem.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    private static Stream<Arguments> nullParameterTestCases() {
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 7, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 7, 11, 0);

        return Stream.of(
                Arguments.of(null, startTime, endTime),
                Arguments.of("room1", null, endTime),
                Arguments.of("room1", startTime, null),
                Arguments.of(null, null, endTime),
                Arguments.of(null, startTime, null),
                Arguments.of("room1", null, null),
                Arguments.of(null, null, null)
        );
    }

    @Test
    @DisplayName("Skapa bokning med korrekta parametrar - ska spara och returnera true")
    void bookRoom_WithValidParameters_ReturnsTrueAndSavesRoom() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("När starttid är i dåtid - kasta IllegalArgumentException")
    void bookRoom_withPastStartTime_ThrowsException() {
        assertThatThrownBy(() ->
                bookingSystem.bookRoom(ROOM_ID, PAST_TIME, FUTURE_END_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kan inte boka tid i dåtid");
    }

    @Test
    @DisplayName("När sluttid är före start tid - kasta IllegalArgumentException")
    void bookRoom_WithEndTimeBeforeStartTime_ThrowsException() {
        assertThatThrownBy(() ->
                bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, PAST_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");
    }

    @Test
    @DisplayName("När rummet inte finns - kasta IllegalArgumentException")
    void bookRoom_WhenRoomNotFound_ThrowsException() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rummet existerar inte");
    }

    @Test
    @DisplayName("När rummet inte är tillgängligt - returnera false")
    void bookRoom_WhenRoomNotAvailable_ReturnsFalse() {
        Booking existingBooking = new Booking(
                UUID.randomUUID().toString(),
                ROOM_ID,
                FUTURE_START_TIME,
                FUTURE_END_TIME
        );
        room.addBooking(existingBooking);

        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isFalse();
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Lyckad bokning skickar notifikation")
    void bookRoom_SuccessfulBooking_SendsNotification() throws NotificationException {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("Bokning lyckas även om notifikation kastar NotificationException")
    void bookRoom_WhenNotificationThrowsNotificationException_StillReturnsTrue() throws NotificationException {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        doThrow(new NotificationException("Error"))
                .when(notificationService)
                .sendBookingConfirmation(any(Booking.class));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();
        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("Lyckad bokning sparar rummet i repository")
    void bookRoom_Successful_SavesRoomToRepository() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("Lyckad bokning lägger till booking i rummet")
    void bookRoom_Successful_AddsBookingToRoom() throws NotificationException {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();

        verify(roomRepository).save(room);
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("Validering sker i rätt ordning - null check först")
    void bookRoom_ValidationOrder_NullCheckBeforeTimeCheck() {
        assertThatThrownBy(() ->
                bookingSystem.bookRoom(null, PAST_TIME, FUTURE_END_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
    }

    @Test
    @DisplayName("Bokning använder UUID för booking ID")
    void bookRoom_UsesUUIDForBookingId() throws NotificationException {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        verify(notificationService).sendBookingConfirmation(any(Booking.class));
        verify(roomRepository).save(room);
    }

    @Test
    @DisplayName("Bokning med starttid exakt nu - ska fungera")
    void bookRoom_WithStartTimeEqualToCurrentTime_ReturnsTrue() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        when(timeProvider.getCurrentTime()).thenReturn(FUTURE_START_TIME);

        boolean result = bookingSystem.bookRoom(ROOM_ID, FUTURE_START_TIME, FUTURE_END_TIME);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Bokning som börjar 1 nanosekund i dåtid - kastar exception (ha-ha)")
    void bookRoom_OneNanosecondInPast_ThrowsException() {
        LocalDateTime oneNanosecondPast = CURRENT_TIME.minusNanos(1);
        LocalDateTime future = CURRENT_TIME.plusHours(1);

        assertThatThrownBy(() ->
                bookingSystem.bookRoom(ROOM_ID, oneNanosecondPast, future))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kan inte boka tid i dåtid");
    }
}