package com.kharjnegar.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kharjnegar_prefs")

data class UserSettings(
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val language: AppLanguage = AppLanguage.FA,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val monthlyBudgetRial: Long = 0L,
    val autoCaptureEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val CURRENCY_UNIT = stringPreferencesKey("currency_unit")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MONTHLY_BUDGET_RIAL = longPreferencesKey("monthly_budget_rial")
        val AUTO_CAPTURE = booleanPreferencesKey("auto_capture")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            currencyUnit = prefs[Keys.CURRENCY_UNIT]?.let {
                runCatching { CurrencyUnit.valueOf(it) }.getOrNull()
            } ?: CurrencyUnit.TOMAN,
            language = prefs[Keys.LANGUAGE]?.let {
                runCatching { AppLanguage.valueOf(it) }.getOrNull()
            } ?: AppLanguage.FA,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            monthlyBudgetRial = prefs[Keys.MONTHLY_BUDGET_RIAL] ?: 0L,
            autoCaptureEnabled = prefs[Keys.AUTO_CAPTURE] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true
        )
    }

    suspend fun getCurrentSettings(): UserSettings = settingsFlow.first()

    suspend fun setCurrencyUnit(unit: CurrencyUnit) {
        context.dataStore.edit { it[Keys.CURRENCY_UNIT] = unit.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setMonthlyBudgetRial(budgetRial: Long) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET_RIAL] = budgetRial.coerceAtLeast(0L) }
    }

    suspend fun setAutoCaptureEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_CAPTURE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }
}
