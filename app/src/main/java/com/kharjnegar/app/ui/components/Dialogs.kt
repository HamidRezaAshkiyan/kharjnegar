package com.kharjnegar.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.data.TransactionEntity
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.JalaliCalendar
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.parser.BankSmsParser
import com.kharjnegar.app.parser.TransactionCategorizer
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.theme.ExpenseRed
import com.kharjnegar.app.ui.theme.IncomeGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddManualTransactionDialog(
    unit: CurrencyUnit,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (IranianBank, TransactionType, Long, Category, String) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf(IranianBank.MELLAT) }
    var selectedCategory by remember { mutableStateOf(Category.FOOD) }

    val popularBanks = remember {
        listOf(
            IranianBank.MELLAT, IranianBank.MELLI, IranianBank.BLU,
            IranianBank.SAMAN, IranianBank.SADERAT, IranianBank.TEJARAT,
            IranianBank.PASARGAD, IranianBank.SEPAH, IranianBank.OTHER
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.addManual(language), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text(Strings.expense(language)) }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = {
                            type = TransactionType.INCOME
                            selectedCategory = Category.SALARY
                        },
                        label = { Text(Strings.income(language)) }
                    )
                }

                val unitLabel = if (unit == CurrencyUnit.TOMAN) Strings.toman(language) else Strings.rial(language)
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch in '۰'..'۹' } },
                    label = { Text("${Strings.amount(language)} ($unitLabel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text(Strings.note(language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(Strings.category(language), style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.title(language), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(Strings.bank(language), style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularBanks.forEach { b ->
                        FilterChip(
                            selected = selectedBank == b,
                            onClick = { selectedBank = b },
                            label = { Text(b.displayName(language), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val normalizedDigits = PersianNormalizer.normalize(amountInput)
                    val entered = normalizedDigits.toLongOrNull() ?: return@TextButton
                    if (entered <= 0L) return@TextButton
                    val rial = if (unit == CurrencyUnit.TOMAN) entered * 10L else entered
                    onSave(selectedBank, type, rial, selectedCategory, noteInput.trim())
                    onDismiss()
                }
            ) {
                Text(Strings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        }
    )
}

@Composable
fun SmsTesterDialog(
    unit: CurrencyUnit,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onImport: (sender: String, body: String) -> Unit
) {
    var sender by remember { mutableStateOf("BankMellat") }
    var body by remember { mutableStateOf("") }
    val parsed = remember(sender, body) {
        if (body.isBlank()) null else BankSmsParser.parse(sender, body, System.currentTimeMillis())
    }
    val isFa = language == AppLanguage.FA

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.testSms(language), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text(if (isFa) "سرشماره / فرستنده (اختیاری)" else "Sender (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(if (isFa) "متن پیامک بانکی را اینجا جای‌گذاری کنید" else "Paste Iranian bank SMS text") },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth()
                )

                if (body.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (parsed == null) {
                                Text(
                                    text = if (isFa) "تراکنش بانکی معتبر شناسایی نشد (یا پیامک رمز پویا است)." else "No valid bank transaction detected (or filtered as OTP).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ExpenseRed
                                )
                            } else {
                                val cat = TransactionCategorizer.categorize(parsed.rawBody, parsed.merchant, parsed.type)
                                Text(
                                    text = "${Strings.bank(language)}: ${parsed.bank.displayName(language)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${Strings.amount(language)}: ${PersianNormalizer.formatAmount(parsed.amountRial, unit, language)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (parsed.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                                parsed.balanceAfterRial?.let { bal ->
                                    Text(
                                        text = "${Strings.balance(language)}: ${PersianNormalizer.formatAmount(bal, unit, language)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "${Strings.category(language)}: ${cat.title(language)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = {
                    onImport(sender, body)
                    onDismiss()
                }
            ) {
                Text(Strings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailDialog(
    tx: TransactionEntity,
    unit: CurrencyUnit,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onUpdate: (TransactionEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    var selectedCategory by remember(tx.id) { mutableStateOf(tx.category) }
    var noteText by remember(tx.id) { mutableStateOf(tx.note.orEmpty()) }
    val jalali = remember(tx.timestampMillis) { JalaliCalendar.fromEpochMillis(tx.timestampMillis) }
    val timeStr = remember(tx.timestampMillis) { JalaliCalendar.formatTime(tx.timestampMillis, language) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(tx.bank.displayName(language), fontWeight = FontWeight.Bold)
                Text(
                    text = "${jalali.formatFull(language)} • $timeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = PersianNormalizer.formatAmount(
                        amountRial = tx.amountRial,
                        unit = unit,
                        language = language,
                        showSign = tx.transactionType == TransactionType.INCOME
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.transactionType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                )

                tx.balanceAfterRial?.let { bal ->
                    Text(
                        text = "${Strings.balance(language)}: ${PersianNormalizer.formatAmount(bal, unit, language)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(Strings.note(language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(Strings.category(language), style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.title(language), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (!tx.rawSmsBody.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(Strings.rawSms(language), style = MaterialTheme.typography.labelMedium)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tx.rawSmsBody,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onDelete(tx.id)
                        onDismiss()
                    }
                ) {
                    Text(Strings.delete(language), color = ExpenseRed)
                }
                TextButton(
                    onClick = {
                        onUpdate(
                            tx.copy(
                                categoryId = selectedCategory.id,
                                note = noteText.trim().ifBlank { null }
                            )
                        )
                        onDismiss()
                    }
                ) {
                    Text(Strings.save(language))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        }
    )
}

@Composable
fun BudgetDialog(
    currentBudgetRial: Long,
    unit: CurrencyUnit,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val initialDisplay = if (currentBudgetRial > 0L) {
        if (unit == CurrencyUnit.TOMAN) (currentBudgetRial / 10L).toString() else currentBudgetRial.toString()
    } else ""
    var input by remember { mutableStateOf(initialDisplay) }
    val unitLabel = if (unit == CurrencyUnit.TOMAN) Strings.toman(language) else Strings.rial(language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.setBudget(language), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter { ch -> ch.isDigit() || ch in '۰'..'۹' } },
                label = { Text("${Strings.monthlyBudget(language)} ($unitLabel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val normalized = PersianNormalizer.normalize(input)
                    val value = normalized.toLongOrNull() ?: 0L
                    val rial = if (unit == CurrencyUnit.TOMAN) value * 10L else value
                    onSave(rial)
                    onDismiss()
                }
            ) {
                Text(Strings.save(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language))
            }
        }
    )
}
