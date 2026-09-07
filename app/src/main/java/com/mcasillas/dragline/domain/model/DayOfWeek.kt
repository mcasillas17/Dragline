package com.mcasillas.dragline.domain.model

enum class DayOfWeek(val shortName: String, val fullName: String) {
    MONDAY("M", "Mon"),
    TUESDAY("T", "Tue"),
    WEDNESDAY("W", "Wed"),
    THURSDAY("T", "Thu"),
    FRIDAY("F", "Fri"),
    SATURDAY("S", "Sat"),
    SUNDAY("S", "Sun");

    companion object {
        val ALL: Set<DayOfWeek> = entries.toSet()
        val WEEKDAYS: Set<DayOfWeek> = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        val WEEKENDS: Set<DayOfWeek> = setOf(SATURDAY, SUNDAY)

        fun fromJavaDayOfWeek(day: java.time.DayOfWeek): DayOfWeek {
            return when (day) {
                java.time.DayOfWeek.MONDAY -> MONDAY
                java.time.DayOfWeek.TUESDAY -> TUESDAY
                java.time.DayOfWeek.WEDNESDAY -> WEDNESDAY
                java.time.DayOfWeek.THURSDAY -> THURSDAY
                java.time.DayOfWeek.FRIDAY -> FRIDAY
                java.time.DayOfWeek.SATURDAY -> SATURDAY
                java.time.DayOfWeek.SUNDAY -> SUNDAY
            }
        }
    }
}
