package com.kharjnegar.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kharjnegar.app.KharjNegarApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group multi-part SMS segments by originating address
        val grouped = LinkedHashMap<String, Pair<StringBuilder, Long>>()
        for (sms in messages) {
            val sender = sms.originatingAddress.orEmpty()
            val bodyPart = sms.messageBody.orEmpty()
            val ts = sms.timestampMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
            val entry = grouped.getOrPut(sender) { StringBuilder() to ts }
            entry.first.append(bodyPart)
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as KharjNegarApp

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = app.userPreferences.getCurrentSettings()
                if (!settings.autoCaptureEnabled) return@launch

                val rules = app.repository.getCustomRuleMap()
                for ((sender, pair) in grouped) {
                    val inserted = app.repository.ingestSms(
                        sender = sender,
                        body = pair.first.toString(),
                        timestampMillis = pair.second,
                        cachedRules = rules
                    )
                    if (inserted != null) {
                        NotificationHelper.showTransactionNotification(context, inserted, settings)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
