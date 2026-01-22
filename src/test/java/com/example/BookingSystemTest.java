package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
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
}