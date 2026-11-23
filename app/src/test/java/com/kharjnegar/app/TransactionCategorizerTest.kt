package com.kharjnegar.app

import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.parser.TransactionCategorizer
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCategorizerTest {

    @Test
    fun categorizesPopularIranianMerchants() {
        assertEquals(
            Category.FOOD,
            TransactionCategorizer.categorize("خرید از اسنپ فود", "اسنپ فود", TransactionType.EXPENSE)
        )
        assertEquals(
            Category.TRANSPORT,
            TransactionCategorizer.categorize("برداشت بابت اسنپ", "اسنپ", TransactionType.EXPENSE)
        )
        assertEquals(
            Category.SHOPPING,
            TransactionCategorizer.categorize("پرداخت دیجی کالا", "دیجی کالا", TransactionType.EXPENSE)
        )
        assertEquals(
            Category.GROCERIES,
            TransactionCategorizer.categorize("فروشگاه افق کوروش", null, TransactionType.EXPENSE)
        )
        assertEquals(
            Category.TELECOM,
            TransactionCategorizer.categorize("بسته اینترنت ایرانسل", null, TransactionType.EXPENSE)
        )
        assertEquals(
            Category.HEALTH,
            TransactionCategorizer.categorize("داروخانه شبانه روزی", null, TransactionType.EXPENSE)
        )
        assertEquals(
            Category.SALARY,
            TransactionCategorizer.categorize("واریز حقوق ماهانه", null, TransactionType.INCOME)
        )
    }

    @Test
    fun customRulesOverrideBuiltInRules() {
        val custom = mapOf("اسنپ" to Category.ENTERTAINMENT)
        assertEquals(
            Category.ENTERTAINMENT,
            TransactionCategorizer.categorize("پرداخت اسنپ", null, TransactionType.EXPENSE, custom)
        )
    }
}
