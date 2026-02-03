package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    private static final String ROOM_ID_1 = "123";
    private static final String ROOM_ID_2 = "456";
    private static final String ROOM_ID_3 = "789";
    private static final String ROOM_NAME_1 = "room-A";
    private static final String ROOM_NAME_2 = "room-B";
    private static final String ROOM_NAME_3 = "room-C";

    private static final String ONGOING_BOOKING_ID = "ongoing-booking";
    private static final String FUTURE_BOOKING_ID = "future-booking-1";
    private static final String NON_EXISTING_BOOKING_ID = "non-existing-booking";

    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 7, 9, 0);
    private static final LocalDateTime FUTURE_START_TIME = CURRENT_TIME.plusHours(1);
    private static final LocalDateTime FUTURE_END_TIME = CURRENT_TIME.plusHours(7);
    private static final LocalDateTime PAST_TIME = CURRENT_TIME.minusHours(5);

    @Mock TimeProvider timeProvider;
    @Mock RoomRepository roomRepository;
    @Mock NotificationService notificationService;
    @InjectMocks BookingSystem bookingSystem;

    private static Booking createBooking(String bookingId, String roomId, LocalDateTime start, LocalDateTime end) {
        return new Booking(bookingId, roomId, start, end);
    }
    private void addBooking(Room room, Booking booking) {
        room.addBooking(booking);
    }
    private void mockAllRooms(Room... rooms) {
        when(roomRepository.findAll()).thenReturn(List.of(rooms));
    }

    @Nested
    class BookRoom {
        private Room firstRoom;

        @BeforeEach
        void setUp() {
            firstRoom = new Room(ROOM_ID_1, ROOM_NAME_1);
            Mockito.lenient().when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
            Mockito.lenient().when(roomRepository.findById(ROOM_ID_1)).thenReturn(Optional.of(firstRoom));
        }

        @ParameterizedTest
        @MethodSource("bookRoomNullTestCases")
        void bookRoom_NullParams_Throws(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
            assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
        }

        static Stream<Arguments> bookRoomNullTestCases() {
            return Stream.of(
                    Arguments.of(null, FUTURE_START_TIME, FUTURE_END_TIME),
                    Arguments.of(ROOM_ID_1, null, FUTURE_END_TIME),
                    Arguments.of(ROOM_ID_1, FUTURE_START_TIME, null),
                    Arguments.of(null, null, FUTURE_END_TIME),
                    Arguments.of(null, FUTURE_START_TIME, null),
                    Arguments.of(ROOM_ID_1, null, null),
                    Arguments.of(null, null, null)
            );
        }

        @Test
        void bookRoom_StartInPast_Throws() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, PAST_TIME, FUTURE_END_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kan inte boka tid i dåtid");
        }

        @Test
        void bookRoom_EndBeforeStart_Throws() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, PAST_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }

        @Test
        void bookRoom_RoomNotFound_Throws() {
            when(roomRepository.findById(ROOM_ID_1)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rummet existerar inte");
        }

        @Test
        void bookRoom_RoomUnavailable_ReturnsFalse() {
            firstRoom.addBooking(createBooking(FUTURE_BOOKING_ID, ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME));
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isFalse();
            verify(roomRepository, never()).save(any());
        }

        @Test
        void bookRoom_Success_ReturnsTrueAndSavesRoom() throws NotificationException {
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            assertThat(firstRoom.isAvailable(FUTURE_START_TIME, FUTURE_END_TIME)).isFalse();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any());
        }

        @Test
        void bookRoom_Success_EvenIfNotificationFails() throws NotificationException {
            doThrow(new NotificationException("Fail"))
                    .when(notificationService).sendBookingConfirmation(any());
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any());
        }
    }


    @Nested
    class GetAvailableRooms {
        private Room firstRoom;
        private Room secondRoom;

        @BeforeEach
        void setUp() {
            firstRoom = new Room(ROOM_ID_1, ROOM_NAME_1);
            secondRoom = new Room(ROOM_ID_2, ROOM_NAME_2);
        }

        @ParameterizedTest
        @MethodSource("nullTimeCases")
        void getAvailableRooms_NullParams_Throws(LocalDateTime startTime, LocalDateTime endTime) {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Måste ange både start- och sluttid");
        }

        static Stream<Arguments> nullTimeCases() {
            return Stream.of(
                    Arguments.of(null, FUTURE_END_TIME),
                    Arguments.of(FUTURE_START_TIME, null),
                    Arguments.of(null, null)
            );
        }

        @Test
        void getAvailableRooms_EndBeforeStart_Throws() {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(FUTURE_END_TIME, FUTURE_START_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }

        @Test
        void getAvailableRooms_FiltersAvailableRooms() {
            secondRoom.addBooking(createBooking(FUTURE_BOOKING_ID, ROOM_ID_2, FUTURE_START_TIME, FUTURE_END_TIME));
            when(roomRepository.findAll()).thenReturn(List.of(firstRoom, secondRoom));
            List<Room> available = bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(available).containsExactly(firstRoom);
        }


        @Nested
        class CancelBooking {
            private Room firstRoom;
            private Room secondRoom;
            private Room thirdRoom;

            @BeforeEach
            void setUp() {
                firstRoom = new Room(ROOM_ID_1, ROOM_NAME_1);
                secondRoom = new Room(ROOM_ID_2, ROOM_NAME_2);
                thirdRoom = new Room(ROOM_ID_3, ROOM_NAME_3);
                Mockito.lenient().when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
            }

            @Test
            void cancelBooking_NullId_Throws() {
                assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Boknings-id kan inte vara null");
            }

            @Test
            void cancelBooking_NoMatch_ReturnsFalse() throws NotificationException {
                mockAllRooms(firstRoom, secondRoom, thirdRoom);
                boolean result = bookingSystem.cancelBooking(NON_EXISTING_BOOKING_ID);
                assertThat(result).isFalse();
                verify(roomRepository).findAll();
                verify(roomRepository, never()).save(any());
                verify(notificationService, never()).sendCancellationConfirmation(any());
            }

            @Test
            void cancelBooking_FutureBooking_Success() throws NotificationException {
                Booking b1 = createBooking(FUTURE_BOOKING_ID, ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
                addBooking(firstRoom, b1);
                mockAllRooms(firstRoom, secondRoom, thirdRoom);
                boolean result = bookingSystem.cancelBooking(FUTURE_BOOKING_ID);
                assertThat(result).isTrue();
                assertThat(firstRoom.hasBooking(FUTURE_BOOKING_ID)).isFalse();
                verify(roomRepository).save(firstRoom);
                verify(notificationService).sendCancellationConfirmation(b1);
            }

            @Test
            void cancelBooking_FutureBooking_NotificationFails_StillReturnsTrue() throws NotificationException {
                Booking b1 = createBooking(FUTURE_BOOKING_ID, ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
                addBooking(firstRoom, b1);
                mockAllRooms(firstRoom);
                doThrow(new NotificationException("Error"))
                        .when(notificationService).sendCancellationConfirmation(b1);
                boolean result = bookingSystem.cancelBooking(FUTURE_BOOKING_ID);
                assertThat(result).isTrue();
                assertThat(firstRoom.hasBooking(FUTURE_BOOKING_ID)).isFalse();
                verify(roomRepository).save(firstRoom);
                verify(notificationService).sendCancellationConfirmation(b1);
            }

            @Test
            void cancelBooking_OngoingBooking_Throws() throws NotificationException {
                Booking ongoing = createBooking(ONGOING_BOOKING_ID, ROOM_ID_1, PAST_TIME, FUTURE_END_TIME);
                addBooking(firstRoom, ongoing);
                mockAllRooms(firstRoom, secondRoom, thirdRoom);
                assertThatThrownBy(() -> bookingSystem.cancelBooking(ONGOING_BOOKING_ID))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
                verify(roomRepository, never()).save(any());
                verify(notificationService, never()).sendCancellationConfirmation(any());
            }
        }
    }
}