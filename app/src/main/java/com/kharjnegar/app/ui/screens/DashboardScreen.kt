package com.kharjnegar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.BankBalanceSummary
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.ui.MainUiState
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.components.CategoryDonutChart
import com.kharjnegar.app.ui.components.DailySpendingBarChart
import com.kharjnegar.app.ui.theme.EmeraldDark
import com.kharjnegar.app.ui.theme.EmeraldPrimary
import com.kharjnegar.app.ui.theme.ExpenseRed
import com.kharjnegar.app.ui.theme.IncomeGreen
import com.kharjnegar.app.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    state: MainUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onScanSmsClick: () -> Unit,
    onAddManualClick: () -> Unit,
    onPasteSmsClick: () -> Unit,
    onLoadSampleClick: () -> Unit,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.settings.language
    val unit = state.settings.currencyUnit

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Switcher Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = if (lang == AppLanguage.FA) Icons.Rounded.ChevronRight else Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous Month"
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.selectedMonth.format(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Strings.transactionsCount(lang, state.monthTransactions.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = if (lang == AppLanguage.FA) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                        contentDescription = "Next Month"
                    )
                }
            }
        }

        // Scanning progress banner
        if (state.scanProgress.isScanning) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val label = if (lang == AppLanguage.FA) {
                            "در حال اسکن پیامک‌ها... (${PersianNormalizer.toPersianDigits(state.scanProgress.scanned.toString())} بررسی / ${PersianNormalizer.toPersianDigits(state.scanProgress.imported.toString())} جدید)"
                        } else {
                            "Scanning SMS... (${state.scanProgress.scanned} checked / ${state.scanProgress.imported} imported)"
                        }
                        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Hero Net Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(EmeraldDark, EmeraldPrimary)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = Strings.netFlow(lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = PersianNormalizer.formatAmount(
                            amountRial = state.netFlowRial,
                            unit = unit,
                            language = lang,
                            showSign = true
                        ),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryPill(
                            title = Strings.totalIncome(lang),
                            amount = PersianNormalizer.formatAmount(state.totalIncomeRial, unit, lang),
                            icon = Icons.Rounded.ArrowDownward,
                            accent = Color(0xFFA7F3D0),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryPill(
                            title = Strings.totalExpense(lang),
                            amount = PersianNormalizer.formatAmount(state.totalExpenseRial, unit, lang),
                            icon = Icons.Rounded.ArrowUpward,
                            accent = Color(0xFFFECACA),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Monthly Budget Progress
                    if (state.settings.monthlyBudgetRial > 0L) {
                        val ratio = (state.totalExpenseRial.toFloat() / state.settings.monthlyBudgetRial.toFloat())
                            .coerceIn(0f, 1f)
                        val pct = ((state.totalExpenseRial.toFloat() / state.settings.monthlyBudgetRial.toFloat()) * 100).toInt()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable { onEditBudgetClick() }
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Strings.monthlyBudget(lang)}: ${PersianNormalizer.formatAmount(state.settings.monthlyBudgetRial, unit, lang)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = if (lang == AppLanguage.FA) "${PersianNormalizer.toPersianDigits(pct.toString())}٪" else "$pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 90) Color(0xFFFECACA) else Color.White
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                color = when {
                                    pct >= 100 -> ExpenseRed
                                    pct >= 80 -> WarningAmber
                                    else -> Color.White
                                },
                                trackColor = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onScanSmsClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.scanSms(lang), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(
                    onClick = onAddManualClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.addManual(lang), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                }
                FilledTonalButton(
                    onClick = onPasteSmsClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(Strings.testSms(lang), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Empty State when no transactions exist at all
        if (state.allTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            text = Strings.emptyTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.emptySubtitle(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = onScanSmsClick) {
                                Text(Strings.scanSms(lang))
                            }
                            OutlinedButton(onClick = onLoadSampleClick) {
                                Text(Strings.loadDemo(lang))
                            }
                        }
                    }
                }
            }
        }

        // Bank Balances Carousel
        if (state.bankBalances.isNotEmpty()) {
            item {
                Text(
                    text = Strings.bankBalances(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.bankBalances) { bankSummary ->
                        BankBalanceCard(summary = bankSummary, unit = unit, language = lang)
                    }
                }
            }
        }

        // Category Donut Chart
        if (state.categoryBreakdowns.isNotEmpty()) {
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
                            text = Strings.categoryBreakdown(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        CategoryDonutChart(
                            breakdowns = state.categoryBreakdowns,
                            totalExpenseRial = state.totalExpenseRial,
                            unit = unit,
                            language = lang
                        )
                    }
                }
            }
        }

        // Daily Spending Bar Chart
        if (state.monthTransactions.isNotEmpty()) {
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
                            text = Strings.dailySpending(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DailySpendingBarChart(
                            dailySpending = state.dailySpending,
                            language = lang
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(accent.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BankBalanceCard(
    summary: BankBalanceSummary,
    unit: CurrencyUnit,
    language: AppLanguage
) {
    val accent = Color(summary.bank.colorHex)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.width(190.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = summary.bank.displayName(language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            summary.accountNumber?.let { acc ->
                Text(
                    text = acc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = PersianNormalizer.formatAmount(summary.latestBalanceRial, unit, language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IncomeGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
