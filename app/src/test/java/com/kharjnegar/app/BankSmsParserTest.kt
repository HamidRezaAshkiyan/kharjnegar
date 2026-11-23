package com.kharjnegar.app

import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.parser.BankSmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsParserTest {

    private val ts = 1_732_389_000_000L

    @Test
    fun parsesBankMellatExpenseAndBalance() {
        val sms = """
            بانک ملت
            حساب 12345678
            برداشت 450,000 ریال
            مانده 12,300,000 ریال
        """.trimIndent()
        val res = BankSmsParser.parse("BankMellat", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.MELLAT, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(450_000L, res.amountRial)
        assertEquals(12_300_000L, res.balanceAfterRial)
        assertEquals("••5678", res.accountNumber)
    }

    @Test
    fun parsesBankMellatSignedAmountFormat() {
        val sms = """
            بانک ملت
            حساب 99887766
            -1,250,000
            مانده: 45,000,000
        """.trimIndent()
        val res = BankSmsParser.parse("98200033", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.MELLAT, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(1_250_000L, res.amountRial)
        assertEquals(45_000_000L, res.balanceAfterRial)
    }

    @Test
    fun parsesBankMellatTrailingPlusIncome() {
        val sms = """
            بانک ملت
            حساب 11223344
            5,000,000+
            مانده 20,000,000
        """.trimIndent()
        val res = BankSmsParser.parse("BankMellat", sms, ts)
        assertNotNull(res)
        assertEquals(TransactionType.INCOME, res!!.type)
        assertEquals(5_000_000L, res.amountRial)
    }

    @Test
    fun parsesBluBankTomanToRial() {
        val sms = """
            بلوبانک
            مبلغ ۳۸۵,۰۰۰ تومان از حساب شما برداشت شد.
            پذیرنده: اسنپ فود
            موجودی: ۱۲,۴۵۰,۰۰۰ تومان
        """.trimIndent()
        val res = BankSmsParser.parse("blubank", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.BLU, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(3_850_000L, res.amountRial)
        assertEquals(124_500_000L, res.balanceAfterRial)
        assertEquals("اسنپ فود", res.merchant)
    }

    @Test
    fun parsesBluBankIncomeToman() {
        val sms = """
            بلوبانک
            مبلغ ۲,۵۰۰,۰۰۰ تومان به حساب شما واریز شد.
            موجودی فعلی: ۱۵,۰۰۰,۰۰۰ تومان
        """.trimIndent()
        val res = BankSmsParser.parse("blubank", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.BLU, res!!.bank)
        assertEquals(TransactionType.INCOME, res.type)
        assertEquals(25_000_000L, res.amountRial)
        assertEquals(150_000_000L, res.balanceAfterRial)
    }

    @Test
    fun parsesBankMelliCardPurchase() {
        val sms = """
            بانک ملی ایران
            کارت 6037****4821
            خرید: 32,000,000 ریال
            پذیرنده: دیجی کالا
            مانده: 128,600,000 ریال
        """.trimIndent()
        val res = BankSmsParser.parse("BMI", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.MELLI, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(32_000_000L, res.amountRial)
        assertEquals(128_600_000L, res.balanceAfterRial)
        assertEquals("••4821", res.accountNumber)
        assertEquals("دیجی کالا", res.merchant)
    }

    @Test
    fun parsesSamanBank() {
        val sms = """
            بانک سامان
            برداشت 4,200,000 ریال از حساب 801-1234
            مانده 84,200,000 ریال
        """.trimIndent()
        val res = BankSmsParser.parse("SamanBank", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.SAMAN, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(4_200_000L, res.amountRial)
        assertEquals(84_200_000L, res.balanceAfterRial)
    }

    @Test
    fun parsesPasargadBank() {
        val sms = """
            بانک پاسارگاد
            برداشت از حساب 201-8841
            مبلغ: 6,500,000 ریال
            مانده: 67,100,000 ریال
        """.trimIndent()
        val res = BankSmsParser.parse("Pasargad", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.PASARGAD, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(6_500_000L, res.amountRial)
        assertEquals(67_100_000L, res.balanceAfterRial)
    }

    @Test
    fun parsesSaderatTejaratSepahKeshavarzi() {
        val saderat = BankSmsParser.parse(
            "BSI",
            "بانک صادرات ایران\nبرداشت: 1,500,000 ریال\nمانده: 9,000,000 ریال",
            ts
        )
        assertEquals(IranianBank.SADERAT, saderat?.bank)
        assertEquals(1_500_000L, saderat?.amountRial)

        val tejarat = BankSmsParser.parse(
            "TejaratBank",
            "بانک تجارت\nواریز: 25,000,000 ریال\nمانده: 55,000,000 ریال",
            ts
        )
        assertEquals(IranianBank.TEJARAT, tejarat?.bank)
        assertEquals(TransactionType.INCOME, tejarat?.type)
        assertEquals(25_000_000L, tejarat?.amountRial)

        val sepah = BankSmsParser.parse(
            "BankSepah",
            "بانک سپه\nبدهکار: 750,000 ریال\nمانده: 4,200,000 ریال",
            ts
        )
        assertEquals(IranianBank.SEPAH, sepah?.bank)
        assertEquals(TransactionType.EXPENSE, sepah?.type)

        val keshavarzi = BankSmsParser.parse(
            "BKI",
            "بانک کشاورزی\nبستانکار: 10,000,000 ریال\nمانده: 30,000,000 ریال",
            ts
        )
        assertEquals(IranianBank.KESHAVARZI, keshavarzi?.bank)
        assertEquals(TransactionType.INCOME, keshavarzi?.type)
    }

    @Test
    fun parsesOtherIranianBanks() {
        val resalat = BankSmsParser.parse(
            "Resalat",
            "بانک قرض الحسنه رسالت\nبرداشت 2,000,000 ریال\nمانده 14,000,000 ریال",
            ts
        )
        assertEquals(IranianBank.RESALAT, resalat?.bank)

        val mehr = BankSmsParser.parse(
            "QMB",
            "بانک قرض الحسنه مهر ایران\nواریز 8,000,000 ریال\nمانده 19,000,000 ریال",
            ts
        )
        assertEquals(IranianBank.MEHR_IRAN, mehr?.bank)

        val wepod = BankSmsParser.parse(
            "Wepod",
            "ویپاد\nمبلغ 120,000 تومان از حساب شما برداشت شد\nموجودی: 1,500,000 تومان",
            ts
        )
        assertEquals(IranianBank.WEPOD, wepod?.bank)
        assertEquals(1_200_000L, wepod?.amountRial)
    }

    @Test
    fun parsesEnglishBankSms() {
        val sms = "Saman Bank\nDr: 500,000 IRR\nBal: 12,000,000 IRR"
        val res = BankSmsParser.parse("SamanBank", sms, ts)
        assertNotNull(res)
        assertEquals(IranianBank.SAMAN, res!!.bank)
        assertEquals(TransactionType.EXPENSE, res.type)
        assertEquals(500_000L, res.amountRial)
        assertEquals(12_000_000L, res.balanceAfterRial)
    }

    @Test
    fun rejectsOtpAndDynamicPasswords() {
        val otp1 = """
            بانک ملت
            رمز پویا: 482910
            مبلغ: 500,000 ریال
            پذیرنده: دیجی کالا
        """.trimIndent()
        assertNull(BankSmsParser.parse("BankMellat", otp1, ts))

        val otp2 = "بلوبانک\nکد تایید ورود شما: 123456\nاین کد را در اختیار دیگران قرار ندهید."
        assertNull(BankSmsParser.parse("blubank", otp2, ts))

        val otp3 = "Bank Saman OTP verification code: 981234 for 200,000 IRR"
        assertNull(BankSmsParser.parse("SamanBank", otp3, ts))
    }

    @Test
    fun rejectsFailedTransactionsAndNonBankSms() {
        val failed = "بانک ملی\nتراکنش ناموفق به مبلغ 1,000,000 ریال\nموجودی کافی نیست"
        assertNull(BankSmsParser.parse("BMI", failed, ts))

        val normalChat = "سلام! امشب ساعت ۸ میام ۵۰۰,۰۰۰ تومان برات بیارم"
        assertNull(BankSmsParser.parse("+989121234567", normalChat, ts))
    }

    @Test
    fun producesStableDeduplicationHash() {
        val sms = "بانک ملت\nبرداشت 100,000 ریال\nمانده 500,000 ریال"
        val r1 = BankSmsParser.parse("BankMellat", sms, ts)
        val r2 = BankSmsParser.parse("bankmellat", sms, ts + 5_000L) // Same minute bucket
        assertNotNull(r1)
        assertNotNull(r2)
        assertEquals(r1!!.smsHash, r2!!.smsHash)
        assertTrue(r1.smsHash.length == 64)
    }
}
