package com.kharjnegar.app

import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.JalaliCalendar
import com.kharjnegar.app.domain.JalaliDate
import com.kharjnegar.app.domain.JalaliYearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JalaliCalendarTest {

    @Test
    fun convertsNowruzDatesAccurately() {
        // 1 Farvardin 1403 = 2024-03-20
        assertEquals(JalaliDate(1403, 1, 1), JalaliCalendar.gregorianToJalali(2024, 3, 20))
        assertEquals(LocalDate.of(2024, 3, 20), JalaliCalendar.jalaliToGregorian(1403, 1, 1))

        // 1 Farvardin 1404 = 2025-03-21 (1403 was a leap year with 30 Esfand!)
        assertEquals(JalaliDate(1404, 1, 1), JalaliCalendar.gregorianToJalali(2025, 3, 21))
        assertEquals(LocalDate.of(2025, 3, 21), JalaliCalendar.jalaliToGregorian(1404, 1, 1))

        // 1 Farvardin 1405 = 2026-03-21
        assertEquals(JalaliDate(1405, 1, 1), JalaliCalendar.gregorianToJalali(2026, 3, 21))
        assertEquals(LocalDate.of(2026, 3, 21), JalaliCalendar.jalaliToGregorian(1405, 1, 1))
    }

    @Test
    fun leapYearsAndMonthLengthsAreCorrect() {
        assertTrue(JalaliCalendar.isLeapYear(1403))
        assertEquals(30, JalaliCalendar.daysInMonth(1403, 12))

        assertFalse(JalaliCalendar.isLeapYear(1404))
        assertEquals(29, JalaliCalendar.daysInMonth(1404, 12))

        assertEquals(31, JalaliCalendar.daysInMonth(1404, 1))
        assertEquals(31, JalaliCalendar.daysInMonth(1404, 6))
        assertEquals(30, JalaliCalendar.daysInMonth(1404, 7))
        assertEquals(30, JalaliCalendar.daysInMonth(1404, 11))
    }

    @Test
    fun roundTripsMultipleYears() {
        var date = LocalDate.of(2020, 1, 1)
        val end = LocalDate.of(2030, 12, 31)
        while (!date.isAfter(end)) {
            val jalali = JalaliCalendar.gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
            val back = JalaliCalendar.jalaliToGregorian(jalali.year, jalali.month, jalali.day)
            assertEquals("Round-trip failed for $date -> $jalali", date, back)
            date = date.plusDays(13)
        }
    }

    @Test
    fun yearMonthNavigationWorks() {
        assertEquals(JalaliYearMonth(1405, 1), JalaliYearMonth(1404, 12).next())
        assertEquals(JalaliYearMonth(1403, 12), JalaliYearMonth(1404, 1).previous())
        assertEquals("فروردین ۱۴۰۴", JalaliYearMonth(1404, 1).format(AppLanguage.FA))
        assertEquals("Farvardin 1404", JalaliYearMonth(1404, 1).format(AppLanguage.EN))
    }
}
