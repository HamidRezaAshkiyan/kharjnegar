package com.kharjnegar.app.sms

import android.content.Context
import android.provider.Telephony
import com.kharjnegar.app.data.TransactionRepository
import com.kharjnegar.app.domain.ScanProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SmsInboxScanner(
    private val context: Context,
    private val repository: TransactionRepository
) {
    /**
     * Scans the device SMS inbox read-only and imports any Iranian bank transaction SMS.
     * @param lookbackDays Number of days back to scan, or null for all SMS messages.
     */
    fun scanInbox(lookbackDays: Int? = 90): Flow<ScanProgress> = flow {
        emit(ScanProgress(isScanning = true))

        val rules = repository.getCustomRuleMap()
        val selection = if (lookbackDays != null) {
            "${Telephony.Sms.DATE} >= ?"
        } else null
        val selectionArgs = if (lookbackDays != null) {
            val cutoff = System.currentTimeMillis() - lookbackDays.toLong() * 86_400_000L
            arrayOf(cutoff.toString())
        } else null

        var scanned = 0
        var imported = 0
        var skipped = 0

        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->
            val addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)

            while (cursor.moveToNext()) {
                scanned++
                val sender = if (addrIdx >= 0) cursor.getString(addrIdx).orEmpty() else ""
                val body = if (bodyIdx >= 0) cursor.getString(bodyIdx).orEmpty() else ""
                val dateMillis = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()

                val inserted = repository.ingestSms(
                    sender = sender,
                    body = body,
                    timestampMillis = dateMillis,
                    cachedRules = rules
                )
                if (inserted != null) {
                    imported++
                } else {
                    skipped++
                }

                if (scanned % 25 == 0) {
                    emit(
                        ScanProgress(
                            isScanning = true,
                            scanned = scanned,
                            imported = imported,
                            skipped = skipped
                        )
                    )
                }
            }
        }

        emit(
            ScanProgress(
                isScanning = false,
                scanned = scanned,
                imported = imported,
                skipped = skipped,
                finished = true
            )
        )
    }.flowOn(Dispatchers.IO)
}
