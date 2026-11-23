package com.kharjnegar.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class JalaliDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<JalaliDate> {

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun monthName(language: AppLanguage): String =
        JalaliCalendar.monthName(month, language)

    fun formatShort(language: AppLanguage): String {
        val raw = "%04d/%02d/%02d".format(year, month, day)
        return if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(raw) else raw
    }

    fun formatDayMonth(language: AppLanguage): String {
        val d = if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(day.toString()) else day.toString()
        return "$d ${monthName(language)}"
    }

    fun formatFull(language: AppLanguage): String {
        val d = if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(day.toString()) else day.toString()
        val y = if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(year.toString()) else year.toString()
        return "$d ${monthName(language)} $y"
    }

    fun formatMonthYear(language: AppLanguage): String {
        val y = if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(year.toString()) else year.toString()
        return "${monthName(language)} $y"
    }
}

data class JalaliYearMonth(
    val year: Int,
    val month: Int // 1..12
) {
    fun next(): JalaliYearMonth =
        if (month == 12) JalaliYearMonth(year + 1, 1) else JalaliYearMonth(year, month + 1)

    fun previous(): JalaliYearMonth =
        if (month == 1) JalaliYearMonth(year - 1, 12) else JalaliYearMonth(year, month - 1)

    fun daysInMonth(): Int = JalaliCalendar.daysInMonth(year, month)

    fun format(language: AppLanguage): String {
        val y = if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(year.toString()) else year.toString()
        return "${JalaliCalendar.monthName(month, language)} $y"
    }

    fun shortLabel(language: AppLanguage): String = JalaliCalendar.monthName(month, language)
}

/**
 * Pure Kotlin Solar Hijri (Jalali / Shamsi) calendar implementation.
 * Uses the standard astronomical/algorithmic Julian Day Number (JDN) conversion.
 */
object JalaliCalendar {

    val TEHRAN_ZONE: ZoneId = ZoneId.of("Asia/Tehran")

    private val PERSIAN_MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val ENGLISH_MONTHS = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad",
        "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar",
        "Dey", "Bahman", "Esfand"
    )

    fun monthName(month: Int, language: AppLanguage): String {
        val idx = (month - 1).coerceIn(0, 11)
        return if (language == AppLanguage.FA) PERSIAN_MONTHS[idx] else ENGLISH_MONTHS[idx]
    }

    fun isLeapYear(jy: Int): Boolean {
        val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
            1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            jp = jm
        }
        var n = jy - jp
        if (jump - n < 6) {
            n = n - jump + ((jump + 4) / 33) * 33
        }
        var leap = (((n + 1) % 33) - 1) % 4
        if (leap == -1) leap = 4
        return leap == 0
    }

    fun daysInMonth(jy: Int, jm: Int): Int = when (jm) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapYear(jy)) 30 else 29
        else -> throw IllegalArgumentException("Invalid Jalali month: $jm")
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jyInput: Int, jm: Int, jd: Int): LocalDate {
        val jy = jyInput + 1595
        var days = -355668 + (365 * jy) + ((jy / 33) * 8) + (((jy % 33) + 3) / 4) + jd +
            if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val salA = intArrayOf(
            0, 31,
            if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28,
            31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm++
        }
        return LocalDate.of(gy, gm, gd)
    }

    fun fromEpochMillis(epochMillis: Long, zoneId: ZoneId = TEHRAN_ZONE): JalaliDate {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(zoneId)
        return gregorianToJalali(zdt.year, zdt.monthValue, zdt.dayOfMonth)
    }

    fun currentYearMonth(zoneId: ZoneId = TEHRAN_ZONE): JalaliYearMonth {
        val now = ZonedDateTime.now(zoneId)
        val j = gregorianToJalali(now.year, now.monthValue, now.dayOfMonth)
        return JalaliYearMonth(j.year, j.month)
    }

    /**
     * Returns [startInclusiveMillis, endExclusiveMillis] for the given Jalali month in Asia/Tehran.
     */
    fun monthBoundsMillis(
        yearMonth: JalaliYearMonth,
        zoneId: ZoneId = TEHRAN_ZONE
    ): Pair<Long, Long> {
        val startLocal = jalaliToGregorian(yearMonth.year, yearMonth.month, 1)
        val next = yearMonth.next()
        val endLocal = jalaliToGregorian(next.year, next.month, 1)
        val startMillis = startLocal.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = endLocal.atStartOfDay(zoneId).toInstant().toEpochMilli()
        return startMillis to endMillis
    }

    fun formatTime(epochMillis: Long, language: AppLanguage, zoneId: ZoneId = TEHRAN_ZONE): String {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(zoneId)
        val raw = "%02d:%02d".format(zdt.hour, zdt.minute)
        return if (language == AppLanguage.FA) PersianNormalizer.toPersianDigits(raw) else raw
    }
}
