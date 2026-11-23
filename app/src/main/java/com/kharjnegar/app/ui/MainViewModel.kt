package com.kharjnegar.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kharjnegar.app.KharjNegarApp
import com.kharjnegar.app.data.CategoryRuleEntity
import com.kharjnegar.app.data.TransactionEntity
import com.kharjnegar.app.data.TransactionRepository
import com.kharjnegar.app.data.UserPreferences
import com.kharjnegar.app.data.UserSettings
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.BankBalanceSummary
import com.kharjnegar.app.domain.Category
import com.kharjnegar.app.domain.CategoryBreakdown
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.DailySpending
import com.kharjnegar.app.domain.IranianBank
import com.kharjnegar.app.domain.JalaliCalendar
import com.kharjnegar.app.domain.JalaliYearMonth
import com.kharjnegar.app.domain.MonthlyTrend
import com.kharjnegar.app.domain.ScanProgress
import com.kharjnegar.app.domain.ThemeMode
import com.kharjnegar.app.domain.TransactionType
import com.kharjnegar.app.sms.SmsInboxScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedMonth: JalaliYearMonth = JalaliCalendar.currentYearMonth(),
    val settings: UserSettings = UserSettings(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val monthTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val totalExpenseRial: Long = 0L,
    val totalIncomeRial: Long = 0L,
    val bankBalances: List<BankBalanceSummary> = emptyList(),
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val bankSpendingBreakdowns: List<Pair<IranianBank, Long>> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val sixMonthTrends: List<MonthlyTrend> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val filterCategory: Category? = null,
    val filterBank: IranianBank? = null,
    val scanProgress: ScanProgress = ScanProgress(),
    val categoryRules: List<CategoryRuleEntity> = emptyList(),
    val snackbarMessage: String? = null
) {
    val netFlowRial: Long get() = totalIncomeRial - totalExpenseRial
}

class MainViewModel(
    private val repository: TransactionRepository,
    private val preferences: UserPreferences,
    private val smsScanner: SmsInboxScanner
) : ViewModel() {

    private val selectedMonthFlow = MutableStateFlow(JalaliCalendar.currentYearMonth())
    private val searchQueryFlow = MutableStateFlow("")
    private val filterTypeFlow = MutableStateFlow<TransactionType?>(null)
    private val filterCategoryFlow = MutableStateFlow<Category?>(null)
    private val filterBankFlow = MutableStateFlow<IranianBank?>(null)
    private val scanProgressFlow = MutableStateFlow(ScanProgress())
    private val snackbarFlow = MutableStateFlow<String?>(null)

    private val filtersFlow = combine(
        searchQueryFlow,
        filterTypeFlow,
        filterCategoryFlow,
        filterBankFlow
    ) { q, t, c, b -> FilterState(q, t, c, b) }

    val uiState: StateFlow<MainUiState> = combine(
        selectedMonthFlow,
        preferences.settingsFlow,
        repository.observeAllTransactions(),
        filtersFlow,
        combine(scanProgressFlow, repository.observeCategoryRules(), snackbarFlow) { s, r, m -> Triple(s, r, m) }
    ) { month, settings, allTxs, filters, extras ->
        val (scanProgress, rules, snackbar) = extras
        val (startMillis, endMillis) = JalaliCalendar.monthBoundsMillis(month)
        val monthTxs = allTxs.filter { it.timestampMillis in startMillis until endMillis }

        val totalExpense = monthTxs
            .filter { it.transactionType == TransactionType.EXPENSE }
            .sumOf { it.amountRial }
        val totalIncome = monthTxs
            .filter { it.transactionType == TransactionType.INCOME }
            .sumOf { it.amountRial }

        // Latest balance per bank+account across ALL transactions
        val bankBalances = allTxs
            .filter { it.balanceAfterRial != null }
            .groupBy { it.bankId to (it.accountNumber ?: "") }
            .values
            .mapNotNull { group ->
                val latest = group.maxByOrNull { it.timestampMillis } ?: return@mapNotNull null
                val bal = latest.balanceAfterRial ?: return@mapNotNull null
                BankBalanceSummary(
                    bank = latest.bank,
                    accountNumber = latest.accountNumber,
                    latestBalanceRial = bal,
                    lastUpdatedMillis = latest.timestampMillis
                )
            }
            .sortedByDescending { it.latestBalanceRial }

        // Category breakdown for current month expenses
        val expenseTxs = monthTxs.filter { it.transactionType == TransactionType.EXPENSE }
        val categoryBreakdowns = expenseTxs
            .groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amountRial }
                val pct = if (totalExpense > 0L) (sum.toFloat() / totalExpense.toFloat()) else 0f
                CategoryBreakdown(
                    category = cat,
                    totalRial = sum,
                    count = list.size,
                    percentage = pct
                )
            }
            .sortedByDescending { it.totalRial }

        // Bank spending breakdown for current month
        val bankSpending = expenseTxs
            .groupBy { it.bank }
            .map { (bank, list) -> bank to list.sumOf { it.amountRial } }
            .sortedByDescending { it.second }

        // Daily spending for selected Jalali month
        val daysCount = month.daysInMonth()
        val dailyMap = monthTxs.groupBy { JalaliCalendar.fromEpochMillis(it.timestampMillis).day }
        val dailyList = (1..daysCount).map { day ->
            val dayTxs = dailyMap[day].orEmpty()
            DailySpending(
                dayOfMonth = day,
                expenseRial = dayTxs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amountRial },
                incomeRial = dayTxs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amountRial }
            )
        }

        // 6-month trend ending at selectedMonth
        val trendMonths = buildList {
            var cur = month
            repeat(6) {
                add(0, cur)
                cur = cur.previous()
            }
        }
        val trends = trendMonths.map { ym ->
            val (s, e) = JalaliCalendar.monthBoundsMillis(ym)
            val mTxs = allTxs.filter { it.timestampMillis in s until e }
            MonthlyTrend(
                yearMonth = ym,
                expenseRial = mTxs.filter { it.transactionType == TransactionType.EXPENSE }.sumOf { it.amountRial },
                incomeRial = mTxs.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amountRial }
            )
        }

        // Filtered transactions for Transactions screen
        val query = filters.query.trim().lowercase()
        val filtered = monthTxs.filter { tx ->
            val matchesType = filters.type == null || tx.transactionType == filters.type
            val matchesCat = filters.category == null || tx.category == filters.category
            val matchesBank = filters.bank == null || tx.bank == filters.bank
            val matchesQuery = query.isEmpty() ||
                tx.bank.nameFa.lowercase().contains(query) ||
                tx.bank.nameEn.lowercase().contains(query) ||
                tx.category.titleFa.lowercase().contains(query) ||
                tx.category.titleEn.lowercase().contains(query) ||
                (tx.merchant?.lowercase()?.contains(query) == true) ||
                (tx.note?.lowercase()?.contains(query) == true) ||
                (tx.rawSmsBody?.lowercase()?.contains(query) == true)
            matchesType && matchesCat && matchesBank && matchesQuery
        }

        MainUiState(
            selectedMonth = month,
            settings = settings,
            allTransactions = allTxs,
            monthTransactions = monthTxs,
            filteredTransactions = filtered,
            totalExpenseRial = totalExpense,
            totalIncomeRial = totalIncome,
            bankBalances = bankBalances,
            categoryBreakdowns = categoryBreakdowns,
            bankSpendingBreakdowns = bankSpending,
            dailySpending = dailyList,
            sixMonthTrends = trends,
            searchQuery = filters.query,
            filterType = filters.type,
            filterCategory = filters.category,
            filterBank = filters.bank,
            scanProgress = scanProgress,
            categoryRules = rules,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    private data class FilterState(
        val query: String,
        val type: TransactionType?,
        val category: Category?,
        val bank: IranianBank?
    )

    fun previousMonth() {
        selectedMonthFlow.value = selectedMonthFlow.value.previous()
    }

    fun nextMonth() {
        selectedMonthFlow.value = selectedMonthFlow.value.next()
    }

    fun setSearchQuery(q: String) {
        searchQueryFlow.value = q
    }

    fun setFilterType(type: TransactionType?) {
        filterTypeFlow.value = type
    }

    fun setFilterCategory(category: Category?) {
        filterCategoryFlow.value = category
    }

    fun setFilterBank(bank: IranianBank?) {
        filterBankFlow.value = bank
    }

    fun clearSnackbar() {
        snackbarFlow.value = null
    }

    fun showMessage(msg: String) {
        snackbarFlow.value = msg
    }

    fun scanSmsInbox(lookbackDays: Int?) {
        viewModelScope.launch {
            runCatching {
                smsScanner.scanInbox(lookbackDays).collect { progress ->
                    scanProgressFlow.value = progress
                    if (progress.finished) {
                        val isFa = uiState.value.settings.language == AppLanguage.FA
                        snackbarFlow.value = if (isFa) {
                            "اسکن کامل شد: ${progress.imported} تراکنش جدید ثبت شد"
                        } else {
                            "Scan complete: ${progress.imported} new transactions imported"
                        }
                    }
                }
            }.onFailure { err ->
                scanProgressFlow.value = ScanProgress(isScanning = false)
                snackbarFlow.value = err.localizedMessage ?: "SMS scan failed"
            }
        }
    }

    fun importPastedSms(sender: String, body: String) {
        viewModelScope.launch {
            val inserted = repository.ingestSms(
                sender = sender.ifBlank { "ManualPaste" },
                body = body,
                timestampMillis = System.currentTimeMillis()
            )
            val isFa = uiState.value.settings.language == AppLanguage.FA
            snackbarFlow.value = if (inserted != null) {
                if (isFa) "تراکنش با موفقیت ثبت شد" else "Transaction imported successfully"
            } else {
                if (isFa) "پیامک معتبر نبود یا قبلاً ثبت شده است" else "SMS not recognized or already imported"
            }
        }
    }

    fun addManualTransaction(
        bank: IranianBank,
        type: TransactionType,
        amountRial: Long,
        category: Category,
        note: String?
    ) {
        viewModelScope.launch {
            repository.addManualTransaction(bank, type, amountRial, category, note)
            val isFa = uiState.value.settings.language == AppLanguage.FA
            snackbarFlow.value = if (isFa) "تراکنش دستی ثبت شد" else "Manual transaction saved"
        }
    }

    fun updateTransaction(entity: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(entity)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun setCurrencyUnit(unit: CurrencyUnit) {
        viewModelScope.launch { preferences.setCurrencyUnit(unit) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { preferences.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setMonthlyBudgetRial(budgetRial: Long) {
        viewModelScope.launch { preferences.setMonthlyBudgetRial(budgetRial) }
    }

    fun setAutoCapture(enabled: Boolean) {
        viewModelScope.launch { preferences.setAutoCaptureEnabled(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotificationsEnabled(enabled) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            val isFa = uiState.value.settings.language == AppLanguage.FA
            snackbarFlow.value = if (isFa) "همه داده‌ها پاک شدند" else "All data cleared"
        }
    }

    suspend fun exportCsvContent(): String = repository.exportCsv()

    suspend fun exportJsonContent(): String = repository.exportJson()

    fun importJsonContent(json: String) {
        viewModelScope.launch {
            runCatching {
                val count = repository.importJson(json)
                val isFa = uiState.value.settings.language == AppLanguage.FA
                snackbarFlow.value = if (isFa) {
                    "$count تراکنش از فایل بکاپ بازیابی شد"
                } else {
                    "Restored $count transactions from backup"
                }
            }.onFailure {
                val isFa = uiState.value.settings.language == AppLanguage.FA
                snackbarFlow.value = if (isFa) "فایل بکاپ نامعتبر است" else "Invalid backup file"
            }
        }
    }

    /**
     * Loads realistic Iranian bank SMS sample transactions so users can test the app
     * even before granting SMS permissions or on an emulator.
     */
    fun loadSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val day = 86_400_000L
            val samples = listOf(
                Triple("BankMellat", "بانک ملت\nحساب 12345678\nواریز 450,000,000 ریال\nبابت حقوق ماهانه\nمانده 512,400,000 ریال", now - 12 * day),
                Triple("blubank", "بلوبانک\nمبلغ ۳۸۵,۰۰۰ تومان از حساب شما برداشت شد.\nپذیرنده: اسنپ فود\nموجودی: ۱۲,۴۵۰,۰۰۰ تومان", now - 10 * day),
                Triple("BankMellat", "بانک ملت\nحساب 12345678\nبرداشت 18,500,000 ریال\nفروشگاه افق کوروش\nمانده 493,900,000 ریال", now - 8 * day),
                Triple("SamanBank", "بانک سامان\nبرداشت 4,200,000 ریال از حساب 801-1234\nبابت اسنپ\nمانده 84,200,000 ریال", now - 6 * day),
                Triple("BMI", "بانک ملی ایران\nکارت 6037****4821\nخرید: 32,000,000 ریال\nپذیرنده: دیجی کالا\nمانده: 128,600,000 ریال", now - 4 * day),
                Triple("blubank", "بلوبانک\nمبلغ ۲۴۰,۰۰۰ تومان از حساب شما برداشت شد.\nپذیرنده: کافه لمیز\nموجودی: ۱۲,۲۱۰,۰۰۰ تومان", now - 3 * day),
                Triple("Pasargad", "بانک پاسارگاد\nبرداشت از حساب 201-8841\nمبلغ: 6,500,000 ریال\nبابت بسته اینترنت ایرانسل\nمانده: 67,100,000 ریال", now - 2 * day),
                Triple("BankMellat", "بانک ملت\nحساب 12345678\nبرداشت 12,000,000 ریال\nپذیرنده: داروخانه دکتر راد\nمانده 481,900,000 ریال", now - 1 * day),
                Triple("SamanBank", "بانک سامان\nواریز 15,000,000 ریال به حساب 801-1234\nبابت سود سپرده\nمانده 99,200,000 ریال", now - 6 * 3_600_000L)
            )
            val rules = repository.getCustomRuleMap()
            var count = 0
            for ((sender, body, ts) in samples) {
                if (repository.ingestSms(sender, body, ts, rules) != null) {
                    count++
                }
            }
            val isFa = uiState.value.settings.language == AppLanguage.FA
            snackbarFlow.value = if (isFa) {
                "$count تراکنش نمونه بارگذاری شد"
            } else {
                "Loaded $count sample transactions"
            }
        }
    }

    companion object {
        fun factory(app: KharjNegarApp): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(
                        repository = app.repository,
                        preferences = app.userPreferences,
                        smsScanner = app.smsScanner
                    ) as T
                }
            }
    }
}
