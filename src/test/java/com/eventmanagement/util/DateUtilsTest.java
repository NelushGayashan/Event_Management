// src/test/java/com/eventmanagement/util/DateUtilsTest.java
package com.eventmanagement.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilsTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2025-05-25T12:00:00Z"),
            ZoneId.systemDefault()
    );

    @Test
    void whenFormatDateTime_withValidDateTime_thenReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 10, 30, 45);
        String formatted = DateUtils.formatDateTime(dateTime);
        assertThat(formatted).isEqualTo("2023-12-25 10:30:45");
    }

    @Test
    void whenFormatDateTime_withMidnight_thenReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        String formatted = DateUtils.formatDateTime(dateTime);
        assertThat(formatted).isEqualTo("2023-01-01 00:00:00");
    }

    @Test
    void whenFormatDateTime_withEndOfYear_thenReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        String formatted = DateUtils.formatDateTime(dateTime);
        assertThat(formatted).isEqualTo("2023-12-31 23:59:59");
    }

    @ParameterizedTest
    @NullSource
    void whenFormatDateTime_withNull_thenReturnNull(LocalDateTime dateTime) {
        String formatted = DateUtils.formatDateTime(dateTime);
        assertThat(formatted).isNull();
    }

    @Test
    void whenParseDateTime_withValidString_thenReturnLocalDateTime() {
        String dateTimeString = "2023-12-25 10:30:45";
        LocalDateTime parsed = DateUtils.parseDateTime(dateTimeString);
        assertThat(parsed).isEqualTo(LocalDateTime.of(2023, 12, 25, 10, 30, 45));
    }

    @Test
    void whenParseDateTime_withMidnightString_thenReturnLocalDateTime() {
        String dateTimeString = "2023-01-01 00:00:00";
        LocalDateTime parsed = DateUtils.parseDateTime(dateTimeString);
        assertThat(parsed).isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0, 0));
    }

    @ParameterizedTest
    @NullSource
    void whenParseDateTime_withNull_thenReturnNull(String dateTimeString) {
        LocalDateTime parsed = DateUtils.parseDateTime(dateTimeString);
        assertThat(parsed).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-date", "2023-13-01 10:30:45", "2023-12-32 10:30:45", ""})
    void whenParseDateTime_withInvalidString_thenThrowException(String invalidDateString) {
        assertThatThrownBy(() -> DateUtils.parseDateTime(invalidDateString))
                .isInstanceOf(Exception.class);
    }

    @Test
    void whenIsInFuture_withFutureDateTime_thenReturnTrue() {
        LocalDateTime futureDateTime = LocalDateTime.now(fixedClock).plusDays(1);
        boolean isInFuture = DateUtils.isInFuture(futureDateTime, fixedClock);
        assertThat(isInFuture).isTrue();
    }

    @Test
    void whenIsInFuture_withFarFutureDateTime_thenReturnTrue() {
        LocalDateTime futureDateTime = LocalDateTime.now(fixedClock).plusYears(10);
        boolean isInFuture = DateUtils.isInFuture(futureDateTime, fixedClock);
        assertThat(isInFuture).isTrue();
    }

    @Test
    void whenIsInFuture_withPastDateTime_thenReturnFalse() {
        LocalDateTime pastDateTime = LocalDateTime.now(fixedClock).minusDays(1);
        boolean isInFuture = DateUtils.isInFuture(pastDateTime, fixedClock);
        assertThat(isInFuture).isFalse();
    }

    @Test
    void whenIsInFuture_withCurrentTime_thenReturnFalse() {
        LocalDateTime currentDateTime = LocalDateTime.now(fixedClock);
        boolean isInFuture = DateUtils.isInFuture(currentDateTime, fixedClock);
        assertThat(isInFuture).isFalse();
    }

    @ParameterizedTest
    @NullSource
    void whenIsInFuture_withNull_thenReturnFalse(LocalDateTime dateTime) {
        boolean isInFuture = DateUtils.isInFuture(dateTime, fixedClock);
        assertThat(isInFuture).isFalse();
    }

    @Test
    void whenIsInPast_withPastDateTime_thenReturnTrue() {
        LocalDateTime pastDateTime = LocalDateTime.now(fixedClock).minusDays(1);
        boolean isInPast = DateUtils.isInPast(pastDateTime, fixedClock);
        assertThat(isInPast).isTrue();
    }

    @Test
    void whenIsInPast_withFarPastDateTime_thenReturnTrue() {
        LocalDateTime pastDateTime = LocalDateTime.now(fixedClock).minusYears(10);
        boolean isInPast = DateUtils.isInPast(pastDateTime, fixedClock);
        assertThat(isInPast).isTrue();
    }

    @Test
    void whenIsInPast_withFutureDateTime_thenReturnFalse() {
        LocalDateTime futureDateTime = LocalDateTime.now(fixedClock).plusDays(1);
        boolean isInPast = DateUtils.isInPast(futureDateTime, fixedClock);
        assertThat(isInPast).isFalse();
    }

    @Test
    void whenIsInPast_withCurrentTime_thenReturnFalse() {
        LocalDateTime currentDateTime = LocalDateTime.now(fixedClock);
        boolean isInPast = DateUtils.isInPast(currentDateTime, fixedClock);
        assertThat(isInPast).isFalse();
    }

    @ParameterizedTest
    @NullSource
    void whenIsInPast_withNull_thenReturnFalse(LocalDateTime dateTime) {
        boolean isInPast = DateUtils.isInPast(dateTime, fixedClock);
        assertThat(isInPast).isFalse();
    }

    @Test
    void whenFormatAndParse_withRoundTrip_thenReturnOriginalDateTime() {
        LocalDateTime originalDateTime = LocalDateTime.of(2023, 6, 15, 14, 30, 25);
        String formatted = DateUtils.formatDateTime(originalDateTime);
        LocalDateTime parsed = DateUtils.parseDateTime(formatted);
        assertThat(parsed).isEqualTo(originalDateTime);
    }

    @Test
    void whenComparingFutureAndPast_withSameDateTime_thenBothReturnFalse() {
        LocalDateTime currentTime = LocalDateTime.now(fixedClock);
        boolean isInFuture = DateUtils.isInFuture(currentTime, fixedClock);
        boolean isInPast = DateUtils.isInPast(currentTime, fixedClock);
        assertThat(isInFuture).isFalse();
        assertThat(isInPast).isFalse();
    }
}
