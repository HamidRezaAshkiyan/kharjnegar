package com.kharjnegar.app.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object PersianNormalizer {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * Converts Persian/Arabic-Indic digits to ASCII digits, normalizes Arabic yeh/kaf to Persian,
     * removes directional marks, and cleans up spacing.
     */
    fun normalize(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
                in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
                'ي', 'ى', 'ئ' -> sb.append(if (ch == 'ئ') 'ئ' else 'ی')
                'ك' -> sb.append('ک')
                'ة' -> sb.append('ه')
                '٬', '،' -> sb.append(',')
                '\u200E', '\u200F', '\u202A', '\u202B', '\u202C', '\u202D', '\u202E', '\uFEFF' -> {
                    // Strip invisible directional markers
                }
                '\u00A0' -> sb.append(' ')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(value: Long, usePersianDigits: Boolean): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatted = DecimalFormat("#,###", symbols).format(value)
        return if (usePersianDigits) toPersianDigits(formatted) else formatted
    }

    fun formatAmount(
        amountRial: Long,
        unit: CurrencyUnit,
        language: AppLanguage,
        showUnit: Boolean = true,
        showSign: Boolean = false
    ): String {
        val isFa = language == AppLanguage.FA
        val displayValue = when (unit) {
            CurrencyUnit.TOMAN -> amountRial / 10L
            CurrencyUnit.RIAL -> amountRial
        }
        val absFormatted = formatNumber(abs(displayValue), isFa)
        val signPrefix = when {
            displayValue < 0 -> "-"
            showSign && displayValue > 0 -> "+"
            else -> ""
        }
        if (!showUnit) return "$signPrefix$absFormatted"

        val unitLabel = when (unit) {
            CurrencyUnit.TOMAN -> if (isFa) "تومان" else "Toman"
            CurrencyUnit.RIAL -> if (isFa) "ریال" else "IRR"
        }
        return "$signPrefix$absFormatted $unitLabel"
    }

    fun formatCompactAmount(
        amountRial: Long,
        unit: CurrencyUnit,
        language: AppLanguage
    ): String {
        val isFa = language == AppLanguage.FA
        val value = when (unit) {
            CurrencyUnit.TOMAN -> amountRial / 10.0
            CurrencyUnit.RIAL -> amountRial.toDouble()
        }
        val absVal = abs(value)
        val sign = if (value < 0) "-" else ""
        val formatted = when {
            absVal >= 1_000_000_000 -> {
                val n = String.format(Locale.US, "%.1f", absVal / 1_000_000_000.0).removeSuffix(".0")
                if (isFa) "$n میلیارد" else "${n}B"
            }
            absVal >= 1_000_000 -> {
                val n = String.format(Locale.US, "%.1f", absVal / 1_000_000.0).removeSuffix(".0")
                if (isFa) "$n میلیون" else "${n}M"
            }
            absVal >= 1_000 -> {
                val n = String.format(Locale.US, "%.0f", absVal / 1_000.0)
                if (isFa) "$n هزار" else "${n}K"
            }
            else -> absVal.toLong().toString()
        }
        val out = "$sign$formatted"
        return if (isFa) toPersianDigits(out) else out
    }
}
