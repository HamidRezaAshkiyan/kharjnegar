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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.data.TransactionEntity
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.JalaliCalendar
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.ui.MainUiState
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.components.icon
import com.kharjnegar.app.ui.theme.ExpenseRed
import com.kharjnegar.app.ui.theme.IncomeGreen

@Composable
fun TransactionsScreen(
    state: MainUiState,
    onSearchChange: (String) -> Unit,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterCategoryChange: (Category?) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.settings.language
    val unit = state.settings.currencyUnit

    val groupedByDay = remember(state.filteredTransactions, lang) {
        state.filteredTransactions.groupBy { tx ->
            JalaliCalendar.fromEpochMillis(tx.timestampMillis).formatFull(lang)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(Strings.searchHint(lang), style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // Type & Category Filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { onFilterTypeChange(null) },
                    label = { Text(Strings.all(lang)) }
                )
            }
            item {
                FilterChip(
                    selected = state.filterType == TransactionType.EXPENSE,
                    onClick = { onFilterTypeChange(TransactionType.EXPENSE) },
                    label = { Text(Strings.expense(lang)) }
                )
            }
            item {
                FilterChip(
                    selected = state.filterType == TransactionType.INCOME,
                    onClick = { onFilterTypeChange(TransactionType.INCOME) },
                    label = { Text(Strings.income(lang)) }
                )
            }
            items(Category.entries) { cat ->
                FilterChip(
                    selected = state.filterCategory == cat,
                    onClick = {
                        onFilterCategoryChange(if (state.filterCategory == cat) null else cat)
                    },
                    label = { Text(cat.title(lang)) }
                )
            }
        }

        if (state.filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.emptyTitle(lang),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                groupedByDay.forEach { (dateLabel, txs) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                    items(txs, key = { it.id }) { tx ->
                        TransactionRowCard(
                            tx = tx,
                            unit = unit,
                            language = lang,
                            onClick = { onTransactionClick(tx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRowCard(
    tx: TransactionEntity,
    unit: CurrencyUnit,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val catColor = Color(tx.category.colorHex)
    val isIncome = tx.transactionType == TransactionType.INCOME
    val timeStr = JalaliCalendar.formatTime(tx.timestampMillis, language)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(catColor.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tx.category.icon(),
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.note?.takeIf { it.isNotBlank() }
                        ?: tx.merchant?.takeIf { it.isNotBlank() }
                        ?: tx.category.title(language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        tx.bank.displayName(language),
                        tx.accountNumber,
                        timeStr
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+" else "-") + PersianNormalizer.formatAmount(
                        amountRial = tx.amountRial,
                        unit = unit,
                        language = language
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
                tx.balanceAfterRial?.let { bal ->
                    Text(
                        text = "${Strings.balance(language)}: ${PersianNormalizer.formatCompactAmount(bal, unit, language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
