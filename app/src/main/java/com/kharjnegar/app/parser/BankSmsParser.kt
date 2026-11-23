package com.kharjnegar.app.parser

import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.ParsedBankSms
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.domain.TransactionType
import java.security.MessageDigest

object BankSmsParser {

    private val REJECT_KEYWORDS = listOf(
        "رمز پویا", "رمزپویا", "رمز دوم", "رمزدوم",
        "رمز یکبار", "رمز یک بار", "رمز عبور",
        "کد تایید", "کد تأیید", "کد ورود", "کد فعال", "کد امنیتی",
        "otp", "verification code", "one-time", "cvv2",
        "ناموفق", "موجودی کافی نیست", "موجودی ناکافی", "عدم موجودی",
        "لغو شد", "رد شد", "خطا در تراکنش", "مسدود",
        "قرعه کشی", "جشنواره"
    )

    private val BANK_CONTEXT_KEYWORDS = listOf(
        "بانک", "حساب", "مانده", "موجودی", "برداشت", "واریز",
        "کارت", "بدهکار", "بستانکار", "ریال", "تومان", "تومن",
        "mande", "bal", "irr"
    )

    private val EXPENSE_KEYWORDS = listOf(
        "برداشت", "خرید", "پرداخت", "کسر", "بدهکار", "خروج",
        "کارمزد", "قسط", "انتقال از", "انتقال وجه", "انتقال به کارت",
        "سند بدهکار", "برداشت وجه", "پرداخت قبض", "شارژ",
        "withdrawal", "purchase", "debit", "dr:"
    )

    private val INCOME_KEYWORDS = listOf(
        "واریز", "بستانکار", "دریافت", "سود", "حقوق",
        "بازگشت وجه", "برگشت وجه", "برگشت تراکنش", "ورود",
        "سند بستانکار", "واریز وجه", "به حساب شما",
        "deposit", "credit", "cr:"
    )

    private val BALANCE_LINE_REGEX = Regex(
        """(?:مانده|موجودی|mande|balance|bal)(?:\s*(?:حساب|فعلی|قابل\s*برداشت|کل))?\s*[:：\-]?\s*([+-]?\d{1,3}(?:,\d{3})+|[+-]?\d+)""",
        RegexOption.IGNORE_CASE
    )

    private val LABELED_AMOUNT_REGEX = Regex(
        """(برداشت|خرید|پرداخت|کسر|بدهکار|خروج|کارمزد|واریز|بستانکار|دریافت|سود|حقوق|مبلغ|dr|cr|debit|credit)(?:\s+(?:وجه|از\s+حساب|به\s+حساب|اینترنتی|کارت))?\s*[:：\-]?\s*([+-]?\d{1,3}(?:,\d{3})+|[+-]?\d{3,15})([+-])?""",
        RegexOption.IGNORE_CASE
    )

    private val AMOUNT_BEFORE_CURRENCY_REGEX = Regex(
        """([+-]?\d{1,3}(?:,\d{3})+|[+-]?\d{3,15})\s*(?:ریال|تومان|تومن|irr|rial|toman)""",
        RegexOption.IGNORE_CASE
    )

    private val SIGNED_AMOUNT_REGEX = Regex(
        """(?:^|[\s:：])([+-])(\d{1,3}(?:,\d{3})+|\d{3,15})(?:$|[\s\n])|(?:^|[\s:：])(\d{1,3}(?:,\d{3})+|\d{3,15})([+-])(?:$|[\s\n])"""
    )

    private val ACCOUNT_REGEX = Regex(
        """(?:حساب|شماره\s*حساب|ش\.?حساب|کارت|شماره\s*کارت|acct?|card)\s*[:：\-]?\s*([0-9\*\-\.]{4,24})""",
        RegexOption.IGNORE_CASE
    )

    private val MASKED_CARD_REGEX = Regex("""\b\d{4}[\*\-]{4,10}(\d{4})\b""")

    private val MERCHANT_REGEX = Regex(
        """(?:پذیرنده|فروشگاه|بابت|شرح|پایانه|نام\s*پذیرنده)\s*[:：\-]?\s*([^\n]{2,48})"""
    )

    fun parse(sender: String, body: String, timestampMillis: Long): ParsedBankSms? {
        if (body.isBlank()) return null
        val normalized = PersianNormalizer.normalize(body).trim()
        val lower = normalized.lowercase()

        // 1. Reject OTPs, failed transactions, and non-transaction notices
        if (REJECT_KEYWORDS.any { lower.contains(it) }) {
            return null
        }

        // 2. Detect bank (or verify bank context for generic Iranian bank SMS)
        val detectedBank = IranianBank.detect(sender, normalized)
        val hasBankContext = BANK_CONTEXT_KEYWORDS.count { lower.contains(it) } >= 2
        if (detectedBank == null && !hasBankContext) {
            return null
        }
        val bank = detectedBank ?: IranianBank.OTHER

        // 3. Determine currency unit (explicit in SMS overrides bank default)
        val unit = detectUnit(lower, bank)

        // 4. Extract remaining balance first so we don't mistake balance for transaction amount
        val balanceRaw = extractBalance(normalized)

        // 5. Remove balance & account segments when searching for transaction amount
        val textWithoutBalance = BALANCE_LINE_REGEX.replace(normalized, " ")

        // 6. Extract transaction amount and type
        val (amountRaw, type) = extractAmountAndType(textWithoutBalance, lower) ?: return null
        if (amountRaw <= 0L) return null

        val multiplier = if (unit == CurrencyUnit.TOMAN) 10L else 1L
        val amountRial = amountRaw * multiplier
        val balanceRial = balanceRaw?.let { it * multiplier }

        val accountNumber = extractAccountSuffix(normalized)
        val merchant = extractMerchant(normalized)
        val hash = computeHash(sender, timestampMillis, normalized)

        return ParsedBankSms(
            bank = bank,
            type = type,
            amountRial = amountRial,
            balanceAfterRial = balanceRial,
            accountNumber = accountNumber,
            merchant = merchant,
            smsHash = hash,
            rawBody = body.trim(),
            sender = sender.trim(),
            timestampMillis = timestampMillis
        )
    }

    private fun detectUnit(lowerBody: String, bank: IranianBank): CurrencyUnit {
        val hasToman = lowerBody.contains("تومان") || lowerBody.contains("تومن") || lowerBody.contains("toman")
        val hasRial = lowerBody.contains("ریال") || lowerBody.contains("rial") || lowerBody.contains("irr")
        return when {
            hasToman && !hasRial -> CurrencyUnit.TOMAN
            hasRial -> CurrencyUnit.RIAL
            else -> bank.defaultUnit
        }
    }

    private fun extractBalance(normalized: String): Long? {
        val match = BALANCE_LINE_REGEX.find(normalized) ?: return null
        return parseDigits(match.groupValues[1])
    }

    private fun extractAmountAndType(
        textWithoutBalance: String,
        fullLower: String
    ): Pair<Long, TransactionType>? {
        val inferredType = inferTypeFromKeywords(fullLower)

        // Strategy A: Labeled amount (e.g., "برداشت: 450,000", "واریز 2,000,000", "مبلغ: 50,000")
        val labeledMatches = LABELED_AMOUNT_REGEX.findAll(textWithoutBalance).toList()
        for (m in labeledMatches) {
            val label = m.groupValues[1].lowercase()
            val numStr = m.groupValues[2]
            val trailingSign = m.groupValues[3]
            val amount = parseDigits(numStr) ?: continue
            if (amount <= 0L) continue

            val type = when {
                numStr.startsWith("-") || trailingSign == "-" -> TransactionType.EXPENSE
                numStr.startsWith("+") || trailingSign == "+" -> TransactionType.INCOME
                label in listOf("واریز", "بستانکار", "دریافت", "سود", "حقوق", "cr", "credit") -> TransactionType.INCOME
                label in listOf("برداشت", "خرید", "پرداخت", "کسر", "بدهکار", "خروج", "کارمزد", "dr", "debit") -> TransactionType.EXPENSE
                else -> inferredType
            }
            if (type != null) {
                return amount to type
            }
        }

        // Strategy B: Explicit signed amount (e.g., "-250,000", "250,000-", "+5,000,000", "5,000,000+")
        for (line in textWithoutBalance.lines()) {
            val trimmed = line.trim()
            if (isAccountOrMetadataLine(trimmed)) continue
            val m = SIGNED_AMOUNT_REGEX.find(trimmed) ?: continue
            val signPrefix = m.groupValues[1]
            val numPrefix = m.groupValues[2]
            val numSuffix = m.groupValues[3]
            val signSuffix = m.groupValues[4]

            val sign = signPrefix.ifEmpty { signSuffix }
            val numText = numPrefix.ifEmpty { numSuffix }
            val amount = parseDigits(numText) ?: continue
            if (amount <= 0L) continue

            val type = if (sign == "+") TransactionType.INCOME else TransactionType.EXPENSE
            return amount to type
        }

        // Strategy C: Amount followed by currency word (e.g., "50,000 تومان از حساب شما برداشت شد")
        if (inferredType != null) {
            val curMatch = AMOUNT_BEFORE_CURRENCY_REGEX.find(textWithoutBalance)
            if (curMatch != null) {
                val amount = parseDigits(curMatch.groupValues[1])
                if (amount != null && amount > 0L) {
                    return amount to inferredType
                }
            }
        }

        return null
    }

    private fun inferTypeFromKeywords(lower: String): TransactionType? {
        val hasExpense = EXPENSE_KEYWORDS.any { lower.contains(it) }
        val hasIncome = INCOME_KEYWORDS.any { lower.contains(it) }
        return when {
            hasIncome && !hasExpense -> TransactionType.INCOME
            hasExpense && !hasIncome -> TransactionType.EXPENSE
            hasExpense && hasIncome -> {
                // Both appear (e.g. "انتقال از ... به ..." or "برداشت ... بابت واریز"); earliest keyword wins
                val firstExpense = EXPENSE_KEYWORDS.mapNotNull { kw ->
                    lower.indexOf(kw).takeIf { it >= 0 }
                }.minOrNull() ?: Int.MAX_VALUE
                val firstIncome = INCOME_KEYWORDS.mapNotNull { kw ->
                    lower.indexOf(kw).takeIf { it >= 0 }
                }.minOrNull() ?: Int.MAX_VALUE
                if (firstIncome < firstExpense) TransactionType.INCOME else TransactionType.EXPENSE
            }
            else -> null
        }
    }

    private fun isAccountOrMetadataLine(line: String): Boolean {
        val l = line.lowercase()
        return l.startsWith("حساب") ||
            l.startsWith("شماره حساب") ||
            l.startsWith("ش.حساب") ||
            l.startsWith("کارت") ||
            l.startsWith("شماره کارت") ||
            l.startsWith("تاریخ") ||
            l.startsWith("ساعت") ||
            l.startsWith("کد پیگیری") ||
            l.startsWith("پیگیری")
    }

    private fun extractAccountSuffix(normalized: String): String? {
        MASKED_CARD_REGEX.find(normalized)?.let { match ->
            return "••${match.groupValues[1]}"
        }
        ACCOUNT_REGEX.find(normalized)?.let { match ->
            val digits = match.groupValues[1].filter { it.isDigit() }
            if (digits.length >= 4) {
                return "••${digits.takeLast(4)}"
            }
        }
        return null
    }

    private fun extractMerchant(normalized: String): String? {
        val match = MERCHANT_REGEX.find(normalized) ?: return null
        val cleaned = match.groupValues[1].trim().trimEnd(',', '.', '،', '-')
        return cleaned.takeIf { it.length >= 2 }
    }

    private fun parseDigits(raw: String): Long? {
        val clean = raw.replace(",", "").replace("+", "").replace("-", "").trim()
        return clean.toLongOrNull()
    }

    fun computeHash(sender: String, timestampMillis: Long, normalizedBody: String): String {
        val minuteBucket = timestampMillis / 60_000L
        val payload = "${sender.trim().lowercase()}|$minuteBucket|${normalizedBody.trim()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
