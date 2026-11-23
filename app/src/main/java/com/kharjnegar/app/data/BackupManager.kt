package com.kharjnegar.app.data

import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.JalaliCalendar
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

object BackupManager {

    private const val BACKUP_VERSION = 1

    fun exportToJson(
        transactions: List<TransactionEntity>,
        rules: List<CategoryRuleEntity>
    ): String {
        val root = JSONObject()
        root.put("app", "KharjNegar")
        root.put("version", BACKUP_VERSION)
        root.put("exportedAtMillis", System.currentTimeMillis())

        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("smsHash", tx.smsHash)
                put("bankId", tx.bankId)
                put("type", tx.type)
                put("amountRial", tx.amountRial)
                if (tx.balanceAfterRial != null) put("balanceAfterRial", tx.balanceAfterRial)
                if (tx.accountNumber != null) put("accountNumber", tx.accountNumber)
                put("categoryId", tx.categoryId)
                if (tx.merchant != null) put("merchant", tx.merchant)
                if (tx.note != null) put("note", tx.note)
                if (tx.rawSmsBody != null) put("rawSmsBody", tx.rawSmsBody)
                if (tx.smsSender != null) put("smsSender", tx.smsSender)
                put("timestampMillis", tx.timestampMillis)
                put("isManual", tx.isManual)
                put("createdAtMillis", tx.createdAtMillis)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val rulesArray = JSONArray()
        for (rule in rules) {
            rulesArray.put(
                JSONObject().apply {
                    put("keyword", rule.keyword)
                    put("categoryId", rule.categoryId)
                }
            )
        }
        root.put("categoryRules", rulesArray)

        return root.toString(2)
    }

    fun importFromJson(json: String): Pair<List<TransactionEntity>, List<CategoryRuleEntity>> {
        val root = JSONObject(json)
        val txList = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()

        for (i in 0 until txArray.length()) {
            val obj = txArray.getJSONObject(i)
            val hash = obj.optString("smsHash", "")
            if (hash.isBlank()) continue
            txList.add(
                TransactionEntity(
                    smsHash = hash,
                    bankId = obj.optString("bankId", "OTHER"),
                    type = obj.optString("type", "EXPENSE"),
                    amountRial = obj.optLong("amountRial", 0L),
                    balanceAfterRial = if (obj.has("balanceAfterRial") && !obj.isNull("balanceAfterRial")) {
                        obj.optLong("balanceAfterRial")
                    } else null,
                    accountNumber = obj.optString("accountNumber").takeIf { it.isNotBlank() },
                    categoryId = obj.optString("categoryId", "OTHER"),
                    merchant = obj.optString("merchant").takeIf { it.isNotBlank() },
                    note = obj.optString("note").takeIf { it.isNotBlank() },
                    rawSmsBody = obj.optString("rawSmsBody").takeIf { it.isNotBlank() },
                    smsSender = obj.optString("smsSender").takeIf { it.isNotBlank() },
                    timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                    isManual = obj.optBoolean("isManual", false),
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                )
            )
        }

        val ruleList = mutableListOf<CategoryRuleEntity>()
        val rulesArray = root.optJSONArray("categoryRules") ?: JSONArray()
        for (i in 0 until rulesArray.length()) {
            val obj = rulesArray.getJSONObject(i)
            val kw = obj.optString("keyword", "").trim()
            val cat = obj.optString("categoryId", "OTHER")
            if (kw.isNotEmpty()) {
                ruleList.add(CategoryRuleEntity(keyword = kw, categoryId = cat))
            }
        }

        return txList to ruleList
    }

    /**
     * Generates a UTF-8 CSV with BOM (\uFEFF) so Microsoft Excel & LibreOffice display Persian cleanly.
     */
    fun exportToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.appendLine("JalaliDate,GregorianISO,Time,BankFa,BankEn,Type,AmountToman,AmountRial,BalanceToman,Account,CategoryFa,CategoryEn,Merchant,Note")
        val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        for (tx in transactions) {
            val jalali = JalaliCalendar.fromEpochMillis(tx.timestampMillis)
            val jalaliStr = jalali.formatShort(AppLanguage.EN)
            val timeStr = JalaliCalendar.formatTime(tx.timestampMillis, AppLanguage.EN)
            val iso = Instant.ofEpochMilli(tx.timestampMillis)
                .atZone(JalaliCalendar.TEHRAN_ZONE)
                .format(isoFormatter)
            val balanceToman = tx.balanceAfterRial?.div(10L)?.toString() ?: ""

            sb.appendLine(
                listOf(
                    jalaliStr,
                    iso,
                    timeStr,
                    tx.bank.nameFa,
                    tx.bank.nameEn,
                    tx.type,
                    (tx.amountRial / 10L).toString(),
                    tx.amountRial.toString(),
                    balanceToman,
                    tx.accountNumber.orEmpty(),
                    tx.category.titleFa,
                    tx.category.titleEn,
                    tx.merchant.orEmpty(),
                    tx.note.orEmpty()
                ).joinToString(",") { escapeCsv(it) }
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!needsQuotes) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }
}
