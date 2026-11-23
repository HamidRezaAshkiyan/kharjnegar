package com.kharjnegar.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.kharjnegar.app.data.TransactionEntity
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.ui.MainViewModel
import com.kharjnegar.app.ui.Strings
import com.kharjnegar.app.ui.components.AddManualTransactionDialog
import com.kharjnegar.app.ui.components.BudgetDialog
import com.kharjnegar.app.ui.components.SmsTesterDialog
import com.kharjnegar.app.ui.components.TransactionDetailDialog
import com.kharjnegar.app.ui.screens.AnalyticsScreen
import com.kharjnegar.app.ui.screens.DashboardScreen
import com.kharjnegar.app.ui.screens.SettingsScreen
import com.kharjnegar.app.ui.screens.TransactionsScreen
import com.kharjnegar.app.ui.theme.KharjNegarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(application as KharjNegarApp)
    }

    private var pendingLookbackDays: Int? = 90
    private var hasPendingScan = false

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val readGranted = grants[Manifest.permission.READ_SMS] == true
        if (readGranted && hasPendingScan) {
            hasPendingScan = false
            viewModel.scanSmsInbox(pendingLookbackDays)
        } else if (!readGranted) {
            val isFa = viewModel.uiState.value.settings.language == AppLanguage.FA
            viewModel.showMessage(
                if (isFa) "مجوز خواندن پیامک داده نشد" else "SMS permission was not granted"
            )
        }
    }

    private val exportCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            runCatching {
                val csv = viewModel.exportCsvContent()
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
                }
                val isFa = viewModel.uiState.value.settings.language == AppLanguage.FA
                viewModel.showMessage(if (isFa) "فایل CSV با موفقیت ذخیره شد" else "CSV exported successfully")
            }.onFailure {
                viewModel.showMessage(it.localizedMessage ?: "Export failed")
            }
        }
    }

    private val exportJsonLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            runCatching {
                val json = viewModel.exportJsonContent()
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                val isFa = viewModel.uiState.value.settings.language == AppLanguage.FA
                viewModel.showMessage(if (isFa) "بکاپ JSON با موفقیت ذخیره شد" else "JSON backup saved")
            }.onFailure {
                viewModel.showMessage(it.localizedMessage ?: "Backup failed")
            }
        }
    }

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                }
                if (!json.isNullOrBlank()) {
                    viewModel.importJsonContent(json)
                }
            }.onFailure {
                viewModel.showMessage(it.localizedMessage ?: "Import failed")
            }
        }
    }

    private fun requestSmsScan(lookbackDays: Int?) {
        val hasRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasRead) {
            viewModel.scanSmsInbox(lookbackDays)
        } else {
            pendingLookbackDays = lookbackDays
            hasPendingScan = true
            val permissions = buildList {
                add(Manifest.permission.READ_SMS)
                add(Manifest.permission.RECEIVE_SMS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            smsPermissionLauncher.launch(permissions)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val lang = state.settings.language
            val unit = state.settings.currencyUnit

            var selectedTab by rememberSaveable { mutableIntStateOf(0) }
            var showAddManual by remember { mutableStateOf(false) }
            var showSmsTester by remember { mutableStateOf(false) }
            var showBudgetDialog by remember { mutableStateOf(false) }
            var selectedTx by remember { mutableStateOf<TransactionEntity?>(null) }

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(state.snackbarMessage) {
                val msg = state.snackbarMessage ?: return@LaunchedEffect
                snackbarHostState.showSnackbar(msg)
                viewModel.clearSnackbar()
            }

            KharjNegarTheme(
                themeMode = state.settings.themeMode,
                language = lang
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = Strings.appTitle(lang),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        if (selectedTab == 0 || selectedTab == 1) {
                            FloatingActionButton(onClick = { showAddManual = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = Strings.addManual(lang))
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = null) },
                                label = { Text(Strings.tabDashboard(lang)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Rounded.ReceiptLong, contentDescription = null) },
                                label = { Text(Strings.tabTransactions(lang)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Rounded.BarChart, contentDescription = null) },
                                label = { Text(Strings.tabAnalytics(lang)) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                label = { Text(Strings.tabSettings(lang)) }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        0 -> DashboardScreen(
                            state = state,
                            onPreviousMonth = viewModel::previousMonth,
                            onNextMonth = viewModel::nextMonth,
                            onScanSmsClick = { requestSmsScan(90) },
                            onAddManualClick = { showAddManual = true },
                            onPasteSmsClick = { showSmsTester = true },
                            onLoadSampleClick = viewModel::loadSampleData,
                            onEditBudgetClick = { showBudgetDialog = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                        1 -> TransactionsScreen(
                            state = state,
                            onSearchChange = viewModel::setSearchQuery,
                            onFilterTypeChange = viewModel::setFilterType,
                            onFilterCategoryChange = viewModel::setFilterCategory,
                            onTransactionClick = { selectedTx = it },
                            modifier = Modifier.padding(innerPadding)
                        )
                        2 -> AnalyticsScreen(
                            state = state,
                            onEditBudgetClick = { showBudgetDialog = true },
                            modifier = Modifier.padding(innerPadding)
                        )
                        else -> SettingsScreen(
                            state = state,
                            onScanSms = { days -> requestSmsScan(days) },
                            onAutoCaptureChange = viewModel::setAutoCapture,
                            onNotificationsChange = viewModel::setNotifications,
                            onCurrencyChange = viewModel::setCurrencyUnit,
                            onLanguageChange = viewModel::setLanguage,
                            onThemeChange = viewModel::setThemeMode,
                            onOpenSmsTester = { showSmsTester = true },
                            onLoadSampleData = viewModel::loadSampleData,
                            onExportCsv = { exportCsvLauncher.launch("kharjnegar-export.csv") },
                            onExportJson = { exportJsonLauncher.launch("kharjnegar-backup.json") },
                            onImportJson = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                            onClearAllData = viewModel::clearAllData,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                if (showAddManual) {
                    AddManualTransactionDialog(
                        unit = unit,
                        language = lang,
                        onDismiss = { showAddManual = false },
                        onSave = viewModel::addManualTransaction
                    )
                }

                if (showSmsTester) {
                    SmsTesterDialog(
                        unit = unit,
                        language = lang,
                        onDismiss = { showSmsTester = false },
                        onImport = viewModel::importPastedSms
                    )
                }

                if (showBudgetDialog) {
                    BudgetDialog(
                        currentBudgetRial = state.settings.monthlyBudgetRial,
                        unit = unit,
                        language = lang,
                        onDismiss = { showBudgetDialog = false },
                        onSave = viewModel::setMonthlyBudgetRial
                    )
                }

                selectedTx?.let { tx ->
                    TransactionDetailDialog(
                        tx = tx,
                        unit = unit,
                        language = lang,
                        onDismiss = { selectedTx = null },
                        onUpdate = viewModel::updateTransaction,
                        onDelete = viewModel::deleteTransaction
                    )
                }
            }
        }
    }
}
