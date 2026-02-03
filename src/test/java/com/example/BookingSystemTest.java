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
    private static final String BOOKING_ID_1 = "future-booking-1";
    private static final String BOOKING_ID_2 = "future-booking-2";
    private static final String BOOKING_ID_3 = "FUTURE-booking-3";
    private static final String NON_EXISTING_BOOKING_ID = "non-existing-booking";

    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 7, 9, 0);
    private static final LocalDateTime FUTURE_START_TIME = CURRENT_TIME.plusHours(1);
    private static final LocalDateTime FUTURE_END_TIME = CURRENT_TIME.plusHours(7);
    private static final LocalDateTime PAST_TIME = CURRENT_TIME.minusHours(5);

    @Mock TimeProvider timeProvider;
    @Mock RoomRepository roomRepository;
    @Mock NotificationService notificationService;
    @InjectMocks BookingSystem bookingSystem;

    @Nested
    @DisplayName("bookRoom metoden")
    class BookRoomTests {
        private Room firstRoom;

        @BeforeEach
        void setUp() {
            firstRoom = new Room(ROOM_ID_1, ROOM_NAME_1);
            Mockito.lenient().when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
            Mockito.lenient().when(roomRepository.findById(ROOM_ID_1)).thenReturn(Optional.of(firstRoom));
        }

        @ParameterizedTest
        @MethodSource("bookRoomNullTestCases")
        @DisplayName("När någon parameter är null - kasta IllegalArgumentException")
        void bookRoom_WithAnyNullParameter_ThrowsException(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
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
        @DisplayName("Skapa bokning med korrekta parametrar - ska spara och returnera true")
        void bookRoom_WithValidParameters_ReturnsTrueAndSavesRoom() throws NotificationException {
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        @DisplayName("När starttid är i dåtid - kasta IllegalArgumentException")
        void bookRoom_withPastStartTime_ThrowsException() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, PAST_TIME, FUTURE_END_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kan inte boka tid i dåtid");
        }

        @Test
        @DisplayName("När sluttid är före start tid - kasta IllegalArgumentException")
        void bookRoom_WithEndTimeBeforeStartTime_ThrowsException() {
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, PAST_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }

        @Test
        @DisplayName("Når rummet inte finns - kasta IllegalArgumentException")
        void bookRoom_WhenRoomNotFound_ThrowsException() {
            when(roomRepository.findById(ROOM_ID_1)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rummet existerar inte");
        }

        @Test
        @DisplayName("När rummet inte är tillgängligt - returnera false")
        void bookRoom_WhenRoomNotAvailable_ReturnsFalse() {
            Booking existingBooking = new Booking(UUID.randomUUID().toString(), ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            firstRoom.addBooking(existingBooking);
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isFalse();
            verify(roomRepository, never()).save(any(Room.class));
        }

        @Test
        @DisplayName("När bokning lyckas - skicka notifikation")
        void bookRoom_SuccessfulBooking_SendsNotification() throws NotificationException {
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        @DisplayName("Bokning lyckas även om notifikation kastar NotificationException")
        void bookRoom_WhenNotificationThrowsNotificationException_StillReturnsTrue() throws NotificationException {
            doThrow(new NotificationException("Error")).when(notificationService).sendBookingConfirmation(any(Booking.class));
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
        }

        @Test
        @DisplayName("När bokning lyckas - lägg till bokning i rum och spara")
        void bookRoom_Successful_AddsBookingToRoom() throws NotificationException {
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
            assertThat(firstRoom.isAvailable(FUTURE_START_TIME, FUTURE_END_TIME)).isFalse();
        }

        @Test
        @DisplayName("Validerar null före tidskontroll")
        void bookRoom_ValidationOrder_NullCheckBeforeTimeCheck() {
            assertThatThrownBy(() ->
                    bookingSystem.bookRoom(null, PAST_TIME, FUTURE_END_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");
        }

        @Test
        @DisplayName("Använder UUID för boknings-id")
        void bookRoom_UsesUUIDForBookingId() throws NotificationException {
            bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            verify(notificationService).sendBookingConfirmation(any(Booking.class));
            verify(roomRepository).save(firstRoom);
        }

        @Test
        @DisplayName("När starttid är exakt nu - acceptera bokning")
        void bookRoom_WithStartTimeEqualToCurrentTime_ReturnsTrue() {
            when(timeProvider.getCurrentTime()).thenReturn(FUTURE_START_TIME);
            boolean result = bookingSystem.bookRoom(ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("När starttid är 1 nanosekund i dåtid - kasta exception")
        void bookRoom_OneNanosecondInPast_ThrowsException() {
            LocalDateTime oneNanosecondPast = CURRENT_TIME.minusNanos(1);
            LocalDateTime future = CURRENT_TIME.plusHours(1);
            assertThatThrownBy(() ->
                    bookingSystem.bookRoom(ROOM_ID_1, oneNanosecondPast, future))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Kan inte boka tid i dåtid");
        }
    }


    @Nested
    @DisplayName("getAvailableRooms metoden")
    class GetAvailableRoomsTests {
        private Room firstRoom;
        private Room secondRoom;

        @BeforeEach
        void setUp() {
            firstRoom = new Room(ROOM_ID_1, ROOM_NAME_1);
            secondRoom = new Room(ROOM_ID_2, ROOM_NAME_2);
        }

        @ParameterizedTest
        @MethodSource("getAvailableRoomsNullTestCases")
        @DisplayName("När start- eller sluttid är null - kasta IllegalArgumentException")
        void getAvailableRooms_WithNullParameters_ThrowsException(LocalDateTime startTime, LocalDateTime endTime) {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Måste ange både start- och sluttid");
        }

        static Stream<Arguments> getAvailableRoomsNullTestCases() {
            return Stream.of(
                    Arguments.of(null, FUTURE_END_TIME),
                    Arguments.of(FUTURE_START_TIME, null),
                    Arguments.of(null, null)
            );
        }

        @Test
        @DisplayName("När sluttid är före starttid - kasta exception")
        void getAvailableRooms_WithEndTimeBeforeStart_ThrowsException() {
            assertThatThrownBy(() -> bookingSystem.getAvailableRooms(FUTURE_END_TIME, FUTURE_START_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Sluttid måste vara efter starttid");
        }

        @Test
        @DisplayName("Anropar roomRepository.findAll() för att hämta alla rum")
        void getAvailableRooms_CallsRepositoryFindAll() {
            when(roomRepository.findAll()).thenReturn(Collections.emptyList());
            bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            verify(roomRepository).findAll();
        }

        @Test
        @DisplayName("Returnerar filtrerad lista på lediga rum under angiven tid")
        void getAvailableRooms_ReturnsFilteredList() {
            secondRoom.addBooking(createBooking("test-uuid-123", ROOM_ID_2, FUTURE_START_TIME, FUTURE_END_TIME));
            when(roomRepository.findAll()).thenReturn(Arrays.asList(firstRoom, secondRoom));
            List<Room> result = bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result)
                    .hasSize(1)
                    .containsExactly(firstRoom)
                    .doesNotContain(secondRoom);
        }

        @Test
        @DisplayName("När repository är tomt - returnerar tom lista")
        void getAvailableRooms_WithEmptyRepository_ReturnsEmptyList() {
            when(roomRepository.findAll()).thenReturn(Collections.emptyList());
            List<Room> result = bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("När alla rum är lediga - returnerar alla rum")
        void getAvailableRooms_WithBothRoomsAvailable_ReturnsBoth() {
            when(roomRepository.findAll()).thenReturn(Arrays.asList(firstRoom, secondRoom));
            List<Room> result = bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result)
                    .hasSize(2)
                    .containsExactlyInAnyOrder(firstRoom, secondRoom);
        }

        @Test
        @DisplayName("När alla rum är upptagna - returnerar tom lista")
        void getAvailableRooms_WithBothRoomsBooked_ReturnsEmpty() {
            firstRoom.addBooking(createBooking("b1", ROOM_ID_1, FUTURE_START_TIME, FUTURE_END_TIME));
            secondRoom.addBooking(createBooking("b2", ROOM_ID_2, FUTURE_START_TIME, FUTURE_END_TIME));
            when(roomRepository.findAll()).thenReturn(Arrays.asList(firstRoom, secondRoom));
            List<Room> result = bookingSystem.getAvailableRooms(FUTURE_START_TIME, FUTURE_END_TIME);
            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("cancelBooking metoden")
    class CancelBookingTests {
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
        @DisplayName("När boknings-id är null - kasta IllegalArgumentException")
        void cancelBooking_WithNullBookingId_ThrowsException() {
            assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Boknings-id kan inte vara null");
        }

        @Test
        @DisplayName("Söker genom alla rum från repository men hittar inget matchande boknings-id - returnera false")
        void cancelBooking_SearchesAllRoomsFromRepository_NoMatchingBookingId_ReturnsFalse() throws
                NotificationException {
            List<Room> allRooms = List.of(firstRoom, secondRoom, thirdRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            boolean result = bookingSystem.cancelBooking(NON_EXISTING_BOOKING_ID);
            assertThat(result).as("Ska returnera false när bokningen inte finns").isFalse();
            verify(roomRepository).findAll();
            verify(roomRepository, never()).save(any(Room.class));
            verify(notificationService, never()).sendCancellationConfirmation(any(Booking.class));
            assertThat(firstRoom.hasBooking(NON_EXISTING_BOOKING_ID)).isFalse();
            assertThat(secondRoom.hasBooking(NON_EXISTING_BOOKING_ID)).isFalse();
            assertThat(thirdRoom.hasBooking(NON_EXISTING_BOOKING_ID)).isFalse();
        }

        @Test
        @DisplayName("När framtida bokning hittas - avboka och returnera true")
        void cancelBooking_FindsFutureBooking_ReturnsTrueAndCancels() throws NotificationException {
            Booking futureBooking = createFutureBooking(BOOKING_ID_1, ROOM_ID_1);
            firstRoom.addBooking(futureBooking);
            List<Room> allRooms = List.of(firstRoom, secondRoom, thirdRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            boolean result = bookingSystem.cancelBooking(BOOKING_ID_1);
            assertThat(result).isTrue();
            verify(roomRepository).findAll();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendCancellationConfirmation(futureBooking);
            assertThat(firstRoom.hasBooking((BOOKING_ID_1))).isFalse();
        }

        @Test
        @DisplayName("När pågående bokning avbokas - kasta IllegalStateException")
        void cancelBooking_AttemptToCancelOngoingBooking_ThrowsException() throws NotificationException {
            Booking ongoingBooking = createOngoingBooking(ONGOING_BOOKING_ID, ROOM_ID_1);
            firstRoom.addBooking(ongoingBooking);
            List<Room> allRooms = List.of(firstRoom, secondRoom, thirdRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            assertThatThrownBy(() -> bookingSystem.cancelBooking(ONGOING_BOOKING_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
            verify(roomRepository, never()).save(any(Room.class));
            verify(notificationService, never()).sendCancellationConfirmation(any(Booking.class));
            assertThat(firstRoom.hasBooking(ONGOING_BOOKING_ID)).isTrue();
        }

        @Test
        @DisplayName("Lyckad avbokning sparar rummet i repository")
        void cancelBooking_SuccessfulCancellation_SavesRoomToRepository() throws NotificationException {
            Booking futureBooking = createFutureBooking(BOOKING_ID_1, ROOM_ID_1);
            firstRoom.addBooking(futureBooking);
            List<Room> allRooms = List.of(firstRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            boolean result = bookingSystem.cancelBooking(BOOKING_ID_1);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendCancellationConfirmation(futureBooking);
        }

        @Test
        @DisplayName("Lyckad avbokning skickar notifikation")
        void cancelBooking_SuccessfulCancellation_SendsNotification() throws NotificationException {
            Booking futureBooking = createFutureBooking(BOOKING_ID_1, ROOM_ID_1);
            firstRoom.addBooking(futureBooking);
            List<Room> allRooms = List.of(firstRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            boolean result = bookingSystem.cancelBooking(BOOKING_ID_1);
            assertThat(result).isTrue();
            verify(notificationService).sendCancellationConfirmation(futureBooking);
        }

        @Test
        @DisplayName("Avbokning lyckas även om notifikation kastar NotificationException")
        void cancelBooking_WhenNotificationThrowsNotificationException_StillReturnsTrue() throws NotificationException {
            Booking futureBooking = createFutureBooking(BOOKING_ID_1, ROOM_ID_1);
            firstRoom.addBooking(futureBooking);
            List<Room> allRooms = List.of(firstRoom);
            when(roomRepository.findAll()).thenReturn(allRooms);
            doThrow(new NotificationException("Error")).when(notificationService).sendCancellationConfirmation(futureBooking);
            boolean result = bookingSystem.cancelBooking(BOOKING_ID_1);
            assertThat(result).isTrue();
            verify(roomRepository).save(firstRoom);
            verify(notificationService).sendCancellationConfirmation(futureBooking);
        }

        @Test
        @DisplayName("Avbokning av framtida bokning påverkar endast rätt rum")
        void cancelBooking_CancelsOnlyBookingInCorrectRoom() {
            Booking booking1 = createFutureBooking(BOOKING_ID_1, ROOM_ID_1);
            Booking booking2 = createFutureBooking(BOOKING_ID_2, ROOM_ID_2);
            Booking booking3 = createFutureBooking(BOOKING_ID_3, ROOM_ID_3);
            firstRoom.addBooking(booking1);
            secondRoom.addBooking(booking2);
            thirdRoom.addBooking(booking3);
            when(roomRepository.findAll()).thenReturn(List.of(firstRoom, secondRoom, thirdRoom));
            boolean result = bookingSystem.cancelBooking(BOOKING_ID_1);
            assertThat(result).isTrue();
            assertThat(firstRoom.hasBooking(BOOKING_ID_1)).isFalse();
            assertThat(secondRoom.hasBooking(BOOKING_ID_2)).isTrue();
            assertThat(thirdRoom.hasBooking(BOOKING_ID_3)).isTrue();
            verify(roomRepository).save(firstRoom);
        }
    }


    private Booking createBooking(String bookingId, String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return new Booking(bookingId, roomId, startTime, endTime);
    }
    private Booking createOngoingBooking(String bookingId, String roomId) {
        return createBooking(bookingId, roomId, PAST_TIME, FUTURE_END_TIME);
    }
    private Booking createFutureBooking(String bookingId, String roomId) {
        return createBooking(bookingId, roomId, FUTURE_START_TIME, FUTURE_END_TIME);
    }
}