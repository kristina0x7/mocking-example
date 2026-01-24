package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class BookingTest {

    @Test
    @DisplayName("Booking skapas med korrekta värden")
    void constructor_WithValidParameters_CreatesBooking() {

        String id = "booking1";
        String roomId = "room1";
        LocalDateTime start = LocalDateTime.of(2026, 1, 7, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 7, 11, 0);

        Booking booking = new Booking(id, roomId, start, end);

        assertThat(booking.getId()).isEqualTo(id);
        assertThat(booking.getRoomId()).isEqualTo(roomId);
        assertThat(booking.getStartTime()).isEqualTo(start);
        assertThat(booking.getEndTime()).isEqualTo(end);
    }

    @Test
    @DisplayName("Booking med null-värden - kastar exception")
    void constructor_WithNullValues_ThrowsException() {
        LocalDateTime validTime = LocalDateTime.now();

        assertThatThrownBy(() -> new Booking(null, "room", validTime, validTime))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Booking("id", null, validTime, validTime))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Booking("id", "room", null, validTime))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Booking("id", "room", validTime, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Booking med ogiltiga tider (slut före start) - kastar exception")
    void constructor_WithInvalidTimes_ThrowsException() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 7, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 7, 10, 0);

        assertThatThrownBy(() -> new Booking("id", "room", start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sluttid måste vara efter starttid");
    }

    @ParameterizedTest
    @MethodSource("overlapScenarios")
    @DisplayName("Testa overlaps() med olika scenarier")
    void overlaps_VariousScenarios_ReturnsCorrectResult(
            String description,
            LocalDateTime bookingStart, LocalDateTime bookingEnd,
            LocalDateTime testStart, LocalDateTime testEnd,
            boolean expectedOverlap) {

        Booking booking = new Booking("id", "room", bookingStart, bookingEnd);

        boolean result = booking.overlaps(testStart, testEnd);

        assertThat(result)
                .as("Scenario: %s", description)
                .isEqualTo(expectedOverlap);
    }

    private static Stream<Arguments> overlapScenarios() {
        LocalDateTime t10 = LocalDateTime.of(2026, 1, 7, 10, 0);
        LocalDateTime t11 = t10.plusHours(1);
        LocalDateTime t12 = t10.plusHours(2);
        LocalDateTime t13 = t10.plusHours(3);

        return Stream.of(
                Arguments.of("Exakt samma tid",
                        t10, t12, t10, t12, true),

                Arguments.of("Ny börjar mitt i existerande",
                        t10, t12, t11, t13, true),

                Arguments.of("Ny slutar mitt i existerande",
                        t11, t13, t10, t12, true),

                Arguments.of("Ingen överlappning (efter)",
                        t10, t11, t12, t13, false),

                Arguments.of("Ingen överlappning (före)",
                        t12, t13, t10, t11, false),

                Arguments.of("Börjar precis när annan slutar",
                        t10, t11, t11, t12, false),

                Arguments.of("Slutar precis när annan börjar",
                        t11, t12, t10, t11, false),

                Arguments.of("1 minut överlappning",
                        t10, t12,
                        t11.plusMinutes(59), t13, true)
        );
    }
}