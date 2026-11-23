package com.kharjnegar.app.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kharjnegar.app.MainActivity
import com.kharjnegar.app.R
import com.kharjnegar.app.data.TransactionEntity
import com.kharjnegar.app.data.UserSettings
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.domain.TransactionType

object NotificationHelper {

    private const val CHANNEL_ID = "kharjnegar_transactions"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "تراکنش‌های بانکی / Bank Transactions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "اعلان محلی برای تراکنش‌های شناسایی‌شده از پیامک بانک‌ها"
        }
        manager.createNotificationChannel(channel)
    }

    fun showTransactionNotification(
        context: Context,
        tx: TransactionEntity,
        settings: UserSettings
    ) {
        if (!settings.notificationsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel(context)

        val isFa = settings.language == AppLanguage.FA
        val amountText = PersianNormalizer.formatAmount(
            amountRial = tx.amountRial,
            unit = settings.currencyUnit,
            language = settings.language
        )
        val typeLabel = when (tx.transactionType) {
            TransactionType.EXPENSE -> if (isFa) "برداشت" else "Expense"
            TransactionType.INCOME -> if (isFa) "واریز" else "Income"
        }
        val title = "$typeLabel $amountText — ${tx.bank.displayName(settings.language)}"
        val balancePart = tx.balanceAfterRial?.let { bal ->
            val balText = PersianNormalizer.formatAmount(bal, settings.currencyUnit, settings.language)
            if (isFa) "مانده: $balText" else "Balance: $balText"
        }
        val body = listOfNotNull(
            tx.category.title(settings.language),
            balancePart
        ).joinToString(" • ")

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            tx.id.toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(tx.id.toInt().coerceAtLeast(1), notification)
        }
    }
}
