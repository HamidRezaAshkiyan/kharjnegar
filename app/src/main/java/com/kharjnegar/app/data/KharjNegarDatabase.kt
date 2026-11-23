package com.kharjnegar.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CategoryRuleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KharjNegarDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryRuleDao(): CategoryRuleDao

    companion object {
        @Volatile
        private var INSTANCE: KharjNegarDatabase? = null

        fun getInstance(context: Context): KharjNegarDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KharjNegarDatabase::class.java,
                    "kharjnegar.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
