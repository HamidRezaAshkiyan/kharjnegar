package com.kharjnegar.app.parser

import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.domain.TransactionType

object TransactionCategorizer {

    private val BUILT_IN_RULES: List<Pair<Category, List<String>>> = listOf(
        Category.FOOD to listOf(
            "اسنپ فود", "اسنپ‌فود", "snappfood", "رستوران", "کافه", "فست فود",
            "پیتزا", "برگر", "کباب", "نانوایی", "شیرینی", "قنادی", "بستنی", "غذا", "قهوه"
        ),
        Category.GROCERIES to listOf(
            "سوپرمارکت", "سوپر مارکت", "هایپر", "افق کوروش", "شهروند", "رفاه",
            "جانبو", "دیلی مارکت", "اکالا", "okala", "میوه", "تره بار", "پروتئین", "لبنیات"
        ),
        Category.TRANSPORT to listOf(
            "اسنپ", "snapp", "تپسی", "tapsi", "تاکسی", "مترو", "بنزین", "جایگاه سوخت",
            "سوخت", "عوارض", "پارکینگ", "اتوبوس", "بلیط", "قطار", "هواپیما", "علی بابا", "مستر بلیط"
        ),
        Category.TELECOM to listOf(
            "ایرانسل", "irancell", "همراه اول", "mci", "رایتل", "rightel", "شاتل",
            "مخابرات", "اینترنت", "بسته اینترنت", "شارژ سیم", "شارژ"
        ),
        Category.BILLS to listOf(
            "قبض", "برق", "آب و فاضلاب", "گاز", "شهرداری", "مالیات", "بیمه", "اجاره"
        ),
        Category.SHOPPING to listOf(
            "دیجی کالا", "دیجی‌کالا", "digikala", "باسلام", "ترب", "تکنولایف",
            "پوشاک", "لباس", "کفش", "فروشگاه", "لوازم", "کتاب", "دیجی پی"
        ),
        Category.HEALTH to listOf(
            "داروخانه", "بیمارستان", "درمانگاه", "کلینیک", "پزشک", "دکتر",
            "آزمایشگاه", "دندانپزشکی", "دارو", "سلامت"
        ),
        Category.ENTERTAINMENT to listOf(
            "سینما", "فیلیمو", "نماوا", "تئاتر", "کنسرت", "باشگاه", "استخر",
            "بازی", "گیم", "سفر", "هتل", "اقامتگاه"
        ),
        Category.FEES to listOf(
            "کارمزد", "قسط", "تسهیلات", "وام", "جریمه", "آبونمان"
        ),
        Category.TRANSFER to listOf(
            "کارت به کارت", "کارت‌به‌کارت", "ساتنا", "پایا", "پل", "انتقال وجه", "انتقال"
        ),
        Category.SALARY to listOf(
            "حقوق", "دستمزد", "یارانه", "سود سپرده", "سود ماهیانه", "سود", "پاداش"
        )
    )

    fun categorize(
        rawBody: String,
        merchant: String?,
        type: TransactionType,
        customRules: Map<String, Category> = emptyMap()
    ): Category {
        val haystack = PersianNormalizer.normalize(
            buildString {
                if (!merchant.isNullOrBlank()) {
                    append(merchant)
                    append(' ')
                }
                append(rawBody)
            }
        ).lowercase()

        // 1. User-defined custom rules take highest priority
        for ((keyword, category) in customRules) {
            val normalizedKw = PersianNormalizer.normalize(keyword).lowercase().trim()
            if (normalizedKw.isNotEmpty() && haystack.contains(normalizedKw)) {
                return category
            }
        }

        // 2. Built-in merchant & keyword rules
        for ((category, keywords) in BUILT_IN_RULES) {
            if (keywords.any { haystack.contains(it) }) {
                return category
            }
        }

        // 3. Fallback based on transaction type
        return if (type == TransactionType.INCOME) Category.SALARY else Category.OTHER
    }
}
