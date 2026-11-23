package com.kharjnegar.app.ui

import com.kharjnegar.app.domain.AppLanguage

object Strings {
    fun appTitle(l: AppLanguage) = if (l == AppLanguage.FA) "خرج‌نگار" else "KharjNegar"
    fun tabDashboard(l: AppLanguage) = if (l == AppLanguage.FA) "داشبورد" else "Dashboard"
    fun tabTransactions(l: AppLanguage) = if (l == AppLanguage.FA) "تراکنش‌ها" else "Transactions"
    fun tabAnalytics(l: AppLanguage) = if (l == AppLanguage.FA) "تحلیل و بودجه" else "Analytics"
    fun tabSettings(l: AppLanguage) = if (l == AppLanguage.FA) "تنظیمات" else "Settings"

    fun totalExpense(l: AppLanguage) = if (l == AppLanguage.FA) "مجموع هزینه" else "Total Expense"
    fun totalIncome(l: AppLanguage) = if (l == AppLanguage.FA) "مجموع درآمد" else "Total Income"
    fun netFlow(l: AppLanguage) = if (l == AppLanguage.FA) "تراز ماهانه" else "Monthly Net"
    fun expense(l: AppLanguage) = if (l == AppLanguage.FA) "هزینه" else "Expense"
    fun income(l: AppLanguage) = if (l == AppLanguage.FA) "درآمد" else "Income"
    fun all(l: AppLanguage) = if (l == AppLanguage.FA) "همه" else "All"

    fun scanSms(l: AppLanguage) = if (l == AppLanguage.FA) "اسکن پیامک" else "Scan SMS"
    fun addManual(l: AppLanguage) = if (l == AppLanguage.FA) "ثبت دستی" else "Add Manual"
    fun testSms(l: AppLanguage) = if (l == AppLanguage.FA) "تست پیامک" else "Paste SMS"
    fun loadDemo(l: AppLanguage) = if (l == AppLanguage.FA) "بارگذاری نمونه آزمایشی" else "Load Sample Data"

    fun bankBalances(l: AppLanguage) = if (l == AppLanguage.FA) "موجودی بانک‌ها" else "Bank Balances"
    fun categoryBreakdown(l: AppLanguage) = if (l == AppLanguage.FA) "هزینه به تفکیک دسته‌بندی" else "Spending by Category"
    fun dailySpending(l: AppLanguage) = if (l == AppLanguage.FA) "روند هزینه روزانه" else "Daily Spending"
    fun sixMonthTrend(l: AppLanguage) = if (l == AppLanguage.FA) "مقایسه ۶ ماه اخیر" else "6-Month Income vs Expense"
    fun monthlyBudget(l: AppLanguage) = if (l == AppLanguage.FA) "بودجه ماهانه" else "Monthly Budget"
    fun setBudget(l: AppLanguage) = if (l == AppLanguage.FA) "تعیین بودجه" else "Set Budget"
    fun noBudgetSet(l: AppLanguage) = if (l == AppLanguage.FA) "بودجه ماهانه تعیین نشده" else "No monthly budget set"
    fun balance(l: AppLanguage) = if (l == AppLanguage.FA) "مانده" else "Balance"

    fun emptyTitle(l: AppLanguage) = if (l == AppLanguage.FA) "هنوز تراکنشی ثبت نشده" else "No transactions yet"
    fun emptySubtitle(l: AppLanguage) = if (l == AppLanguage.FA) {
        "پیامک‌های بانکی خود را ۱۰۰٪ آفلاین اسکن کنید یا به صورت دستی تراکنش ثبت نمایید."
    } else {
        "Scan your Iranian bank SMS 100% offline, paste an SMS, or add a transaction manually."
    }

    fun searchHint(l: AppLanguage) = if (l == AppLanguage.FA) "جستجو در بانک، توضیحات یا متن پیامک..." else "Search bank, note, or SMS text..."
    fun offlineBadgeTitle(l: AppLanguage) = if (l == AppLanguage.FA) "۱۰۰٪ آفلاین و متن‌باز" else "100% Offline & Open-Source"
    fun offlineBadgeDesc(l: AppLanguage) = if (l == AppLanguage.FA) {
        "این برنامه هیچ مجوز اینترنتی ندارد. تمام پیامک‌ها و تراکنش‌ها فقط روی گوشی شما پردازش و ذخیره می‌شوند."
    } else {
        "Zero INTERNET permission declared. Your bank SMS and financial data never leave your device."
    }

    fun smsPermissions(l: AppLanguage) = if (l == AppLanguage.FA) "اسکن پیامک‌های بانکی" else "Bank SMS Scanner"
    fun last30Days(l: AppLanguage) = if (l == AppLanguage.FA) "۳۰ روز" else "30 Days"
    fun last90Days(l: AppLanguage) = if (l == AppLanguage.FA) "۹۰ روز" else "90 Days"
    fun lastYear(l: AppLanguage) = if (l == AppLanguage.FA) "۱ سال" else "1 Year"
    fun allSms(l: AppLanguage) = if (l == AppLanguage.FA) "همه" else "All"

    fun autoCapture(l: AppLanguage) = if (l == AppLanguage.FA) "ثبت خودکار پیامک‌های جدید" else "Auto-capture incoming bank SMS"
    fun notifications(l: AppLanguage) = if (l == AppLanguage.FA) "اعلان محلی پس از هر تراکنش" else "Local transaction notifications"
    fun currencyUnit(l: AppLanguage) = if (l == AppLanguage.FA) "واحد پول" else "Currency Unit"
    fun toman(l: AppLanguage) = if (l == AppLanguage.FA) "تومان" else "Toman"
    fun rial(l: AppLanguage) = if (l == AppLanguage.FA) "ریال" else "Rial"
    fun languageLabel(l: AppLanguage) = if (l == AppLanguage.FA) "زبان برنامه" else "Language"
    fun themeLabel(l: AppLanguage) = if (l == AppLanguage.FA) "پوسته ظاهری" else "Theme"
    fun themeSystem(l: AppLanguage) = if (l == AppLanguage.FA) "سیستم" else "System"
    fun themeLight(l: AppLanguage) = if (l == AppLanguage.FA) "روشن" else "Light"
    fun themeDark(l: AppLanguage) = if (l == AppLanguage.FA) "تیره" else "Dark"

    fun backupAndExport(l: AppLanguage) = if (l == AppLanguage.FA) "پشتیبان‌گیری و خروجی" else "Backup & Export"
    fun exportCsv(l: AppLanguage) = if (l == AppLanguage.FA) "خروجی اکسل (CSV)" else "Export CSV"
    fun exportJson(l: AppLanguage) = if (l == AppLanguage.FA) "ذخیره بکاپ (JSON)" else "Export JSON Backup"
    fun importJson(l: AppLanguage) = if (l == AppLanguage.FA) "بازیابی بکاپ (JSON)" else "Import JSON Backup"
    fun clearAllData(l: AppLanguage) = if (l == AppLanguage.FA) "پاک کردن همه داده‌ها" else "Clear All Data"

    fun save(l: AppLanguage) = if (l == AppLanguage.FA) "ذخیره" else "Save"
    fun cancel(l: AppLanguage) = if (l == AppLanguage.FA) "انصراف" else "Cancel"
    fun delete(l: AppLanguage) = if (l == AppLanguage.FA) "حذف" else "Delete"
    fun amount(l: AppLanguage) = if (l == AppLanguage.FA) "مبلغ" else "Amount"
    fun note(l: AppLanguage) = if (l == AppLanguage.FA) "توضیحات" else "Note"
    fun bank(l: AppLanguage) = if (l == AppLanguage.FA) "بانک" else "Bank"
    fun category(l: AppLanguage) = if (l == AppLanguage.FA) "دسته‌بندی" else "Category"
    fun rawSms(l: AppLanguage) = if (l == AppLanguage.FA) "متن اصلی پیامک" else "Original SMS"
    fun spendingByBank(l: AppLanguage) = if (l == AppLanguage.FA) "هزینه به تفکیک بانک" else "Spending by Bank"
    fun transactionsCount(l: AppLanguage, count: Int) = if (l == AppLanguage.FA) {
        "${com.kharjnegar.app.domain.PersianNormalizer.toPersianDigits(count.toString())} تراکنش"
    } else {
        "$count transactions"
    }
}
