package com.kharjnegar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.BuildConfig
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.ThemeMode
import com.kharjnegar.app.ui.MainUiState
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.theme.ExpenseRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: MainUiState,
    onScanSms: (lookbackDays: Int?) -> Unit,
    onAutoCaptureChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onCurrencyChange: (CurrencyUnit) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onOpenSmsTester: () -> Unit,
    onLoadSampleData: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.settings.language
    var showClearConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Privacy Shield Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = Strings.offlineBadgeTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = Strings.offlineBadgeDesc(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // SMS Scanner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Strings.smsPermissions(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = { onScanSms(30) }) { Text(Strings.last30Days(lang)) }
                        OutlinedButton(onClick = { onScanSms(90) }) { Text(Strings.last90Days(lang)) }
                        OutlinedButton(onClick = { onScanSms(365) }) { Text(Strings.lastYear(lang)) }
                        OutlinedButton(onClick = { onScanSms(null) }) { Text(Strings.allSms(lang)) }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.autoCapture(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.settings.autoCaptureEnabled,
                            onCheckedChange = onAutoCaptureChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.notifications(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = state.settings.notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                }
            }
        }

        // Display Preferences Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(Strings.currencyUnit(lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.settings.currencyUnit == CurrencyUnit.TOMAN,
                            onClick = { onCurrencyChange(CurrencyUnit.TOMAN) },
                            label = { Text(Strings.toman(lang)) }
                        )
                        FilterChip(
                            selected = state.settings.currencyUnit == CurrencyUnit.RIAL,
                            onClick = { onCurrencyChange(CurrencyUnit.RIAL) },
                            label = { Text(Strings.rial(lang)) }
                        )
                    }

                    Text(Strings.languageLabel(lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.settings.language == AppLanguage.FA,
                            onClick = { onLanguageChange(AppLanguage.FA) },
                            label = { Text("فارسی (RTL)") }
                        )
                        FilterChip(
                            selected = state.settings.language == AppLanguage.EN,
                            onClick = { onLanguageChange(AppLanguage.EN) },
                            label = { Text("English (LTR)") }
                        )
                    }

                    Text(Strings.themeLabel(lang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.settings.themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeChange(ThemeMode.SYSTEM) },
                            label = { Text(Strings.themeSystem(lang)) }
                        )
                        FilterChip(
                            selected = state.settings.themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeChange(ThemeMode.LIGHT) },
                            label = { Text(Strings.themeLight(lang)) }
                        )
                        FilterChip(
                            selected = state.settings.themeMode == ThemeMode.DARK,
                            onClick = { onThemeChange(ThemeMode.DARK) },
                            label = { Text(Strings.themeDark(lang)) }
                        )
                    }
                }
            }
        }

        // Tools & Backup Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = Strings.backupAndExport(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = onOpenSmsTester,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.testSms(lang))
                    }

                    OutlinedButton(
                        onClick = onLoadSampleData,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.loadDemo(lang))
                    }

                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.exportCsv(lang))
                    }

                    OutlinedButton(
                        onClick = onExportJson,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.exportJson(lang))
                    }

                    OutlinedButton(
                        onClick = onImportJson,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.importJson(lang))
                    }

                    HorizontalDivider()

                    TextButton(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = ExpenseRed)
                        Spacer(Modifier.width(8.dp))
                        Text(Strings.clearAllData(lang), color = ExpenseRed)
                    }
                }
            }
        }

        // Version Footer
        item {
            Text(
                text = "KharjNegar v${BuildConfig.VERSION_NAME} • MIT Open-Source • 100% Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(Strings.clearAllData(lang), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (lang == AppLanguage.FA) {
                        "آیا مطمئن هستید که می‌خواهید تمام تراکنش‌های ذخیره‌شده را پاک کنید؟ (پیامک‌های گوشی شما دست‌نخورده باقی می‌مانند)"
                    } else {
                        "Delete all saved transactions in KharjNegar? (Your phone's SMS inbox will not be modified.)"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearConfirm = false
                    }
                ) {
                    Text(Strings.delete(lang), color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(Strings.cancel(lang))
                }
            }
        )
    }
}
