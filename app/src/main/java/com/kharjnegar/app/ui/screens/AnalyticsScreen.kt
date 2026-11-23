package com.kharjnegar.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.ui.MainUiState
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.components.MonthlyTrendBarChart
import com.kharjnegar.app.ui.theme.ExpenseRed
import com.kharjnegar.app.ui.theme.IncomeGreen
import com.kharjnegar.app.ui.theme.WarningAmber

@Composable
fun AnalyticsScreen(
    state: MainUiState,
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
        // Monthly Budget Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.monthlyBudget(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = onEditBudgetClick) {
                            Text(Strings.setBudget(lang))
                        }
                    }

                    if (state.settings.monthlyBudgetRial <= 0L) {
                        Text(
                            text = Strings.noBudgetSet(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val budget = state.settings.monthlyBudgetRial
                        val ratio = (state.totalExpenseRial.toFloat() / budget.toFloat()).coerceIn(0f, 1f)
                        val pct = ((state.totalExpenseRial.toFloat() / budget.toFloat()) * 100).toInt()
                        val remaining = (budget - state.totalExpenseRial).coerceAtLeast(0L)

                        Text(
                            text = "${PersianNormalizer.formatAmount(state.totalExpenseRial, unit, lang)} / ${PersianNormalizer.formatAmount(budget, unit, lang)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { ratio },
                            color = when {
                                pct >= 100 -> ExpenseRed
                                pct >= 80 -> WarningAmber
                                else -> IncomeGreen
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = if (lang == AppLanguage.FA) {
                                "باقیمانده بودجه: ${PersianNormalizer.formatAmount(remaining, unit, lang)}"
                            } else {
                                "Remaining: ${PersianNormalizer.formatAmount(remaining, unit, lang)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6-Month Trend Chart
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
                        text = Strings.sixMonthTrend(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(color = IncomeGreen, label = Strings.income(lang))
                        LegendDot(color = ExpenseRed, label = Strings.expense(lang))
                    }
                    MonthlyTrendBarChart(
                        trends = state.sixMonthTrends,
                        language = lang
                    )
                }
            }
        }

        // Category Breakdown List
        if (state.categoryBreakdowns.isNotEmpty()) {
            item {
                Text(
                    text = Strings.categoryBreakdown(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.categoryBreakdowns) { item ->
                val color = Color(item.category.colorHex)
                val pctInt = (item.percentage * 100).toInt()
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.category.title(lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = PersianNormalizer.formatAmount(item.totalRial, unit, lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { item.percentage.coerceIn(0f, 1f) },
                            color = color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Strings.transactionsCount(lang, item.count),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lang == AppLanguage.FA) "${PersianNormalizer.toPersianDigits(pctInt.toString())}٪" else "$pctInt%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Spending by Bank
        if (state.bankSpendingBreakdowns.isNotEmpty()) {
            item {
                Text(
                    text = Strings.spendingByBank(lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.bankSpendingBreakdowns) { (bank, total) ->
                val ratio = if (state.totalExpenseRial > 0L) (total.toFloat() / state.totalExpenseRial.toFloat()) else 0f
                val bankColor = Color(bank.colorHex)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(bank.displayName(lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(PersianNormalizer.formatAmount(total, unit, lang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            color = bankColor,
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
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
