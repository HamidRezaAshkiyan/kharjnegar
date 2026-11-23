package com.kharjnegar.app

import com.kharjnegar.app.data.BackupManager
import com.kharjnegar.app.data.CategoryRuleEntity
import com.kharjnegar.app.data.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun jsonExportAndImportRoundTripPreservesData() {
        val tx = TransactionEntity(
            smsHash = "abc123",
            bankId = "MELLAT",
            type = "EXPENSE",
            amountRial = 2_500_000L,
            balanceAfterRial = 10_000_000L,
            accountNumber = "••1234",
            categoryId = "FOOD",
            merchant = "کافه لمیز",
            note = "قهوه",
            rawSmsBody = "بانک ملت\nبرداشت 2,500,000 ریال",
            smsSender = "BankMellat",
            timestampMillis = 1_732_389_000_000L
        )
        val rule = CategoryRuleEntity(keyword = "لمیز", categoryId = "FOOD")

        val json = BackupManager.exportToJson(listOf(tx), listOf(rule))
        val (restoredTxs, restoredRules) = BackupManager.importFromJson(json)

        assertEquals(1, restoredTxs.size)
        assertEquals("abc123", restoredTxs[0].smsHash)
        assertEquals(2_500_000L, restoredTxs[0].amountRial)
        assertEquals(10_000_000L, restoredTxs[0].balanceAfterRial)
        assertEquals("کافه لمیز", restoredTxs[0].merchant)

        assertEquals(1, restoredRules.size)
        assertEquals("لمیز", restoredRules[0].keyword)
    }

    @Test
    fun csvExportIncludesUtf8BomAndColumns() {
        val tx = TransactionEntity(
            smsHash = "xyz",
            bankId = "BLU",
            type = "EXPENSE",
            amountRial = 1_000_000L,
            balanceAfterRial = 5_000_000L,
            accountNumber = null,
            categoryId = "GROCERIES",
            merchant = "افق کوروش",
            note = null,
            rawSmsBody = null,
            smsSender = "blubank",
            timestampMillis = 1_732_389_000_000L
        )
        val csv = BackupManager.exportToCsv(listOf(tx))
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("JalaliDate"))
        assertTrue(csv.contains("Blu Bank"))
        assertTrue(csv.contains("100000,1000000"))
    }
}
