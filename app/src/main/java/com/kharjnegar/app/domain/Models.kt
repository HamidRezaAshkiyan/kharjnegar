package com.kharjnegar.app.domain

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class CurrencyUnit {
    TOMAN,
    RIAL
}

enum class AppLanguage {
    FA,
    EN
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class Category(
    val id: String,
    val titleFa: String,
    val titleEn: String,
    val colorHex: Long
) {
    FOOD("FOOD", "غذا و رستوران", "Food & Dining", 0xFFEF4444),
    GROCERIES("GROCERIES", "سوپرمارکت", "Groceries", 0xFFF97316),
    TRANSPORT("TRANSPORT", "حمل‌ونقل", "Transport", 0xFF3B82F6),
    SHOPPING("SHOPPING", "خرید", "Shopping", 0xFFA855F7),
    BILLS("BILLS", "قبوض و خدمات", "Bills & Utilities", 0xFF06B6D4),
    TELECOM("TELECOM", "اینترنت و شارژ", "Telecom", 0xFF6366F1),
    HEALTH("HEALTH", "درمان و سلامت", "Health", 0xFFEC4899),
    ENTERTAINMENT("ENTERTAINMENT", "تفریح و سرگرمی", "Entertainment", 0xFFEAB308),
    TRANSFER("TRANSFER", "انتقال وجه", "Transfer", 0xFF64748B),
    SALARY("SALARY", "حقوق و درآمد", "Salary & Income", 0xFF10B981),
    FEES("FEES", "کارمزد و قسط", "Bank Fees & Loans", 0xFF78716C),
    OTHER("OTHER", "سایر", "Other", 0xFF94A3B8);

    fun title(language: AppLanguage): String = if (language == AppLanguage.FA) titleFa else titleEn

    companion object {
        fun fromId(id: String): Category = entries.firstOrNull { it.id == id } ?: OTHER
    }
}

data class ParsedBankSms(
    val bank: IranianBank,
    val type: TransactionType,
    val amountRial: Long,
    val balanceAfterRial: Long?,
    val accountNumber: String?,
    val merchant: String?,
    val smsHash: String,
    val rawBody: String,
    val sender: String,
    val timestampMillis: Long
)

data class BankBalanceSummary(
    val bank: IranianBank,
    val accountNumber: String?,
    val latestBalanceRial: Long,
    val lastUpdatedMillis: Long
)

data class CategoryBreakdown(
    val category: Category,
    val totalRial: Long,
    val count: Int,
    val percentage: Float
)

data class DailySpending(
    val dayOfMonth: Int,
    val expenseRial: Long,
    val incomeRial: Long
)

data class MonthlyTrend(
    val yearMonth: JalaliYearMonth,
    val expenseRial: Long,
    val incomeRial: Long
)

data class ScanProgress(
    val isScanning: Boolean = false,
    val scanned: Int = 0,
    val imported: Int = 0,
    val skipped: Int = 0,
    val finished: Boolean = false
)
