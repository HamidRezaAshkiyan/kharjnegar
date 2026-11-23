package com.kharjnegar.app

import android.app.Application
import com.kharjnegar.app.data.KharjNegarDatabase
import com.kharjnegar.app.data.TransactionRepository
import com.kharjnegar.app.data.UserPreferences
import com.kharjnegar.app.sms.NotificationHelper
import com.kharjnegar.app.sms.SmsInboxScanner

class KharjNegarApp : Application() {

    val database: KharjNegarDatabase by lazy { KharjNegarDatabase.getInstance(this) }
    val repository: TransactionRepository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            categoryRuleDao = database.categoryRuleDao()
        )
    }
    val userPreferences: UserPreferences by lazy { UserPreferences(this) }
    val smsScanner: SmsInboxScanner by lazy { SmsInboxScanner(this, repository) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
