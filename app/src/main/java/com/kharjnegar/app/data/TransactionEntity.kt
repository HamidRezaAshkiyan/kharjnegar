package com.kharjnegar.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["smsHash"], unique = true),
        Index(value = ["timestampMillis"]),
        Index(value = ["bankId"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val smsHash: String,
    val bankId: String,
    val type: String, // "EXPENSE" or "INCOME"
    val amountRial: Long,
    val balanceAfterRial: Long?,
    val accountNumber: String?,
    val categoryId: String,
    val merchant: String?,
    val note: String?,
    val rawSmsBody: String?,
    val smsSender: String?,
    val timestampMillis: Long,
    val isManual: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val bank: IranianBank get() = IranianBank.fromId(bankId)
    val transactionType: TransactionType get() =
        if (type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
    val category: Category get() = Category.fromId(categoryId)
}

@Entity(
    tableName = "category_rules",
    indices = [Index(value = ["keyword"], unique = true)]
)
data class CategoryRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val categoryId: String
)
