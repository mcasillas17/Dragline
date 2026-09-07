package com.mcasillas.dragline.scheduling

import com.mcasillas.dragline.domain.model.DayOfWeek
import com.mcasillas.dragline.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class FixedTimeCalculatorTest {

    private lateinit var calculator: FixedTimeCalculator
    private val zone = ZoneId.of("America/New_York")

    @Before
    fun setup() {
        calculator = FixedTimeCalculator()
    }

    @Test
    fun `one-time alarm before target time triggers today`() {
        // Monday, Sep 7, 2026, 06:00 AM
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 7),
            LocalTime.of(6, 0),
            zone
        )
        val schedule = Schedule.FixedTime(time = LocalTime.of(7, 30), repeatDays = emptySet())

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        assertEquals(LocalDate.of(2026, 9, 7), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(7, 30), nextTrigger.toLocalTime())
    }

    @Test
    fun `one-time alarm after target time triggers tomorrow`() {
        // Monday, Sep 7, 2026, 08:00 AM
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 7),
            LocalTime.of(8, 0),
            zone
        )
        val schedule = Schedule.FixedTime(time = LocalTime.of(7, 30), repeatDays = emptySet())

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        assertEquals(LocalDate.of(2026, 9, 8), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(7, 30), nextTrigger.toLocalTime())
    }

    @Test
    fun `repeating weekday alarm on Friday morning triggers Friday`() {
        // Friday, Sep 11, 2026, 06:30 AM
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 11),
            LocalTime.of(6, 30),
            zone
        )
        val schedule = Schedule.FixedTime(
            time = LocalTime.of(7, 0),
            repeatDays = DayOfWeek.WEEKDAYS
        )

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        assertEquals(LocalDate.of(2026, 9, 11), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(7, 0), nextTrigger.toLocalTime())
    }

    @Test
    fun `repeating weekday alarm on Friday afternoon skips weekend to Monday`() {
        // Friday, Sep 11, 2026, 08:30 AM
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 11),
            LocalTime.of(8, 30),
            zone
        )
        val schedule = Schedule.FixedTime(
            time = LocalTime.of(7, 0),
            repeatDays = DayOfWeek.WEEKDAYS
        )

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        // Following Monday is Sep 14, 2026
        assertEquals(LocalDate.of(2026, 9, 14), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(7, 0), nextTrigger.toLocalTime())
    }

    @Test
    fun `repeating weekend alarm on Wednesday triggers Saturday`() {
        // Wednesday, Sep 9, 2026, 10:00 AM
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 9),
            LocalTime.of(10, 0),
            zone
        )
        val schedule = Schedule.FixedTime(
            time = LocalTime.of(9, 0),
            repeatDays = DayOfWeek.WEEKENDS
        )

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        // Saturday is Sep 12, 2026
        assertEquals(LocalDate.of(2026, 9, 12), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(9, 0), nextTrigger.toLocalTime())
    }

    @Test
    fun `alarm scheduled at midnight 00_00 triggers next occurrence correctly`() {
        val fromTime = ZonedDateTime.of(
            LocalDate.of(2026, 9, 7),
            LocalTime.of(23, 30),
            zone
        )
        val schedule = Schedule.FixedTime(time = LocalTime.of(0, 0), repeatDays = emptySet())

        val nextTrigger = calculator.calculateNextTrigger(schedule, fromTime)

        assertEquals(LocalDate.of(2026, 9, 8), nextTrigger.toLocalDate())
        assertEquals(LocalTime.of(0, 0), nextTrigger.toLocalTime())
    }
}
