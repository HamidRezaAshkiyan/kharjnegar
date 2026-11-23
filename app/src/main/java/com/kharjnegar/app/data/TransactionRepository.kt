package com.kharjnegar.app.data

import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.ParsedBankSms
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.parser.BankSmsParser
import com.kharjnegar.app.parser.TransactionCategorizer
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryRuleDao: CategoryRuleDao
) {
    fun observeAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeCategoryRules(): Flow<List<CategoryRuleEntity>> = categoryRuleDao.observeAll()

    suspend fun getCustomRuleMap(): Map<String, Category> {
        return categoryRuleDao.getAll().associate { it.keyword to Category.fromId(it.categoryId) }
    }

    /**
     * Parses a raw SMS and inserts it if valid & not already stored.
     * Returns the inserted TransactionEntity, or null if rejected or duplicate.
     */
    suspend fun ingestSms(
        sender: String,
        body: String,
        timestampMillis: Long,
        cachedRules: Map<String, Category>? = null
    ): TransactionEntity? {
        val parsed = BankSmsParser.parse(sender, body, timestampMillis) ?: return null
        return insertParsedSms(parsed, cachedRules ?: getCustomRuleMap())
    }

    suspend fun insertParsedSms(
        parsed: ParsedBankSms,
        customRules: Map<String, Category>
    ): TransactionEntity? {
        val category = TransactionCategorizer.categorize(
            rawBody = parsed.rawBody,
            merchant = parsed.merchant,
            type = parsed.type,
            customRules = customRules
        )
        val entity = TransactionEntity(
            smsHash = parsed.smsHash,
            bankId = parsed.bank.id,
            type = parsed.type.name,
            amountRial = parsed.amountRial,
            balanceAfterRial = parsed.balanceAfterRial,
            accountNumber = parsed.accountNumber,
            categoryId = category.id,
            merchant = parsed.merchant,
            note = null,
            rawSmsBody = parsed.rawBody,
            smsSender = parsed.sender,
            timestampMillis = parsed.timestampMillis,
            isManual = false
        )
        val rowId = transactionDao.insertIgnore(entity)
        return if (rowId > 0L) entity.copy(id = rowId) else null
    }

    suspend fun addManualTransaction(
        bank: IranianBank,
        type: TransactionType,
        amountRial: Long,
        category: Category,
        note: String?,
        timestampMillis: Long = System.currentTimeMillis()
    ) {
        val entity = TransactionEntity(
            smsHash = "manual_${UUID.randomUUID()}",
            bankId = bank.id,
            type = type.name,
            amountRial = amountRial,
            balanceAfterRial = null,
            accountNumber = null,
            categoryId = category.id,
            merchant = note?.takeIf { it.isNotBlank() },
            note = note?.takeIf { it.isNotBlank() },
            rawSmsBody = null,
            smsSender = null,
            timestampMillis = timestampMillis,
            isManual = true
        )
        transactionDao.insertIgnore(entity)
    }

    suspend fun updateTransaction(entity: TransactionEntity) {
        transactionDao.update(entity)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        categoryRuleDao.deleteAll()
    }

    suspend fun addCategoryRule(keyword: String, category: Category) {
        val cleaned = keyword.trim()
        if (cleaned.isNotEmpty()) {
            categoryRuleDao.upsert(CategoryRuleEntity(keyword = cleaned, categoryId = category.id))
        }
    }

    suspend fun deleteCategoryRule(rule: CategoryRuleEntity) {
        categoryRuleDao.delete(rule)
    }

    suspend fun exportJson(): String {
        val txs = transactionDao.getAll()
        val rules = categoryRuleDao.getAll()
        return BackupManager.exportToJson(txs, rules)
    }

    suspend fun exportCsv(): String {
        val txs = transactionDao.getAll()
        return BackupManager.exportToCsv(txs)
    }

    suspend fun importJson(json: String): Int {
        val (txs, rules) = BackupManager.importFromJson(json)
        val inserted = transactionDao.insertAllIgnore(txs)
        rules.forEach { categoryRuleDao.upsert(it) }
        return inserted.count { it > 0L }
    }
}
