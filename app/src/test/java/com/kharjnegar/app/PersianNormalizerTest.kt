package com.kharjnegar.app

import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.PersianNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class PersianNormalizerTest {

    @Test
    fun normalizesPersianAndArabicDigitsAndLetters() {
        val raw = "مبلغ ١٢٣٬٤٥٦ تومان از بانك ملي"
        assertEquals("مبلغ 123,456 تومان از بانک ملی", PersianNormalizer.normalize(raw))
    }

    @Test
    fun formatsTomanAndRialCorrectly() {
        assertEquals(
            "۱۲۵,۰۰۰ تومان",
            PersianNormalizer.formatAmount(1_250_000L, CurrencyUnit.TOMAN, AppLanguage.FA)
        )
        assertEquals(
            "125,000 Toman",
            PersianNormalizer.formatAmount(1_250_000L, CurrencyUnit.TOMAN, AppLanguage.EN)
        )
        assertEquals(
            "1,250,000 IRR",
            PersianNormalizer.formatAmount(1_250_000L, CurrencyUnit.RIAL, AppLanguage.EN)
        )
    }

    @Test
    fun formatsCompactAmounts() {
        assertEquals("۱.۵ میلیون", PersianNormalizer.formatCompactAmount(15_000_000L, CurrencyUnit.TOMAN, AppLanguage.FA))
        assertEquals("250K", PersianNormalizer.formatCompactAmount(2_500_000L, CurrencyUnit.TOMAN, AppLanguage.EN))
    }
}
