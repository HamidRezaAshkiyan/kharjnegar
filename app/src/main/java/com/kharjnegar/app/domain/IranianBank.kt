package com.kharjnegar.app.domain

enum class IranianBank(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val colorHex: Long,
    val defaultUnit: CurrencyUnit = CurrencyUnit.RIAL,
    val senderKeywords: List<String> = emptyList(),
    val bodyKeywords: List<String> = emptyList()
) {
    MELLAT(
        id = "MELLAT",
        nameFa = "بانک ملت",
        nameEn = "Bank Mellat",
        colorHex = 0xFFB91C1C,
        senderKeywords = listOf("mellat", "bankmellat", "98200033", "200033", "300033"),
        bodyKeywords = listOf("بانک ملت", "ملت", "bank mellat")
    ),
    MELLI(
        id = "MELLI",
        nameFa = "بانک ملی ایران",
        nameEn = "Bank Melli Iran",
        colorHex = 0xFF0E7490,
        senderKeywords = listOf("bmi", "melli", "bankmelli", "98300094", "300094", "98200094"),
        bodyKeywords = listOf("بانک ملی", "بانک ملي", "بام ملی", "bank melli")
    ),
    BLU(
        id = "BLU",
        nameFa = "بلوبانک",
        nameEn = "Blu Bank",
        colorHex = 0xFF2563EB,
        defaultUnit = CurrencyUnit.TOMAN,
        senderKeywords = listOf("blu", "blubank", "98200060", "98900060"),
        bodyKeywords = listOf("بلوبانک", "بلو بانک", "blubank", "از بلو", "به بلو", "حساب بلو")
    ),
    SAMAN(
        id = "SAMAN",
        nameFa = "بانک سامان",
        nameEn = "Saman Bank",
        colorHex = 0xFF0284C7,
        senderKeywords = listOf("saman", "samanbank", "98200072", "200072", "300072"),
        bodyKeywords = listOf("بانک سامان", "سامان", "saman bank")
    ),
    SADERAT(
        id = "SADERAT",
        nameFa = "بانک صادرات",
        nameEn = "Bank Saderat",
        colorHex = 0xFF1D4ED8,
        senderKeywords = listOf("bsi", "saderat", "98200069", "200069", "300069"),
        bodyKeywords = listOf("بانک صادرات", "صادرات ایران", "سپهر صادرات", "bank saderat")
    ),
    TEJARAT(
        id = "TEJARAT",
        nameFa = "بانک تجارت",
        nameEn = "Bank Tejarat",
        colorHex = 0xFF1E3A8A,
        senderKeywords = listOf("tejarat", "tejaratbank", "98200024", "200024", "300024"),
        bodyKeywords = listOf("بانک تجارت", "تجارت", "tejarat")
    ),
    SEPAH(
        id = "SEPAH",
        nameFa = "بانک سپه",
        nameEn = "Bank Sepah",
        colorHex = 0xFFD97706,
        senderKeywords = listOf("sepah", "banksepah", "98200015", "200015", "300015"),
        bodyKeywords = listOf("بانک سپه", "سپه", "bank sepah")
    ),
    PASARGAD(
        id = "PASARGAD",
        nameFa = "بانک پاسارگاد",
        nameEn = "Bank Pasargad",
        colorHex = 0xFFB45309,
        senderKeywords = listOf("pasargad", "bpi", "98200050", "200050", "300050"),
        bodyKeywords = listOf("بانک پاسارگاد", "پاسارگاد", "pasargad")
    ),
    WEPOD(
        id = "WEPOD",
        nameFa = "ویپاد",
        nameEn = "Wepod",
        colorHex = 0xFF7C3AED,
        senderKeywords = listOf("wepod"),
        bodyKeywords = listOf("ویپاد", "وی پاد", "wepod")
    ),
    KESHAVARZI(
        id = "KESHAVARZI",
        nameFa = "بانک کشاورزی",
        nameEn = "Bank Keshavarzi",
        colorHex = 0xFF15803D,
        senderKeywords = listOf("bki", "keshavarzi", "98200091", "200091", "300091"),
        bodyKeywords = listOf("بانک کشاورزی", "کشاورزی", "keshavarzi")
    ),
    REFAH(
        id = "REFAH",
        nameFa = "بانک رفاه",
        nameEn = "Refah Bank",
        colorHex = 0xFF0369A1,
        senderKeywords = listOf("refah", "98200013", "200013"),
        bodyKeywords = listOf("بانک رفاه", "رفاه کارگران", "refah")
    ),
    MASKAN(
        id = "MASKAN",
        nameFa = "بانک مسکن",
        nameEn = "Bank Maskan",
        colorHex = 0xFFEA580C,
        senderKeywords = listOf("maskan", "bankmaskan", "98200014", "200014"),
        bodyKeywords = listOf("بانک مسکن", "مسکن", "maskan")
    ),
    PARSIAN(
        id = "PARSIAN",
        nameFa = "بانک پارسیان",
        nameEn = "Parsian Bank",
        colorHex = 0xFFC2410C,
        senderKeywords = listOf("parsian", "pec", "98200054", "200054"),
        bodyKeywords = listOf("بانک پارسیان", "پارسیان", "parsian")
    ),
    SHAHR(
        id = "SHAHR",
        nameFa = "بانک شهر",
        nameEn = "Shahr Bank",
        colorHex = 0xFFDC2626,
        senderKeywords = listOf("shahr", "shahrbank", "98200061", "200061"),
        bodyKeywords = listOf("بانک شهر", "شهرنت", "shahr bank")
    ),
    AYANDEH(
        id = "AYANDEH",
        nameFa = "بانک آینده",
        nameEn = "Ayandeh Bank",
        colorHex = 0xFF9333EA,
        senderKeywords = listOf("ayandeh", "ba24", "98200062"),
        bodyKeywords = listOf("بانک آینده", "آینده", "ayandeh")
    ),
    RESALAT(
        id = "RESALAT",
        nameFa = "بانک رسالت",
        nameEn = "Resalat Bank",
        colorHex = 0xFF0D9488,
        senderKeywords = listOf("resalat", "rqbank", "98200070"),
        bodyKeywords = listOf("بانک رسالت", "قرض الحسنه رسالت", "رسالت", "resalat")
    ),
    MEHR_IRAN(
        id = "MEHR_IRAN",
        nameFa = "بانک مهر ایران",
        nameEn = "Mehr Iran Bank",
        colorHex = 0xFF059669,
        senderKeywords = listOf("mehr", "qmb", "mehriran", "98200060"),
        bodyKeywords = listOf("مهر ایران", "قرض الحسنه مهر", "mehr iran")
    ),
    SINA(
        id = "SINA",
        nameFa = "بانک سینا",
        nameEn = "Sina Bank",
        colorHex = 0xFF2563EB,
        senderKeywords = listOf("sina", "sinabank"),
        bodyKeywords = listOf("بانک سینا", "سینا", "sina bank")
    ),
    EGHTESAD_NOVIN(
        id = "EGHTESAD_NOVIN",
        nameFa = "بانک اقتصاد نوین",
        nameEn = "Eghtesad Novin",
        colorHex = 0xFF6D28D9,
        senderKeywords = listOf("enbank", "eghtesad"),
        bodyKeywords = listOf("اقتصاد نوین", "اقتصادنوین", "enbank")
    ),
    KARAFARIN(
        id = "KARAFARIN",
        nameFa = "بانک کارآفرین",
        nameEn = "Karafarin Bank",
        colorHex = 0xFF16A34A,
        senderKeywords = listOf("karafarin"),
        bodyKeywords = listOf("بانک کارآفرین", "کارآفرین", "karafarin")
    ),
    DEY(
        id = "DEY",
        nameFa = "بانک دی",
        nameEn = "Bank Dey",
        colorHex = 0xFF0891B2,
        senderKeywords = listOf("dey", "bankdey"),
        bodyKeywords = listOf("بانک دی", "bank dey")
    ),
    GARDESHGARI(
        id = "GARDESHGARI",
        nameFa = "بانک گردشگری",
        nameEn = "Tourism Bank",
        colorHex = 0xFFCA8A04,
        senderKeywords = listOf("tourism", "gardeshgari"),
        bodyKeywords = listOf("بانک گردشگری", "گردشگری", "tourism bank")
    ),
    SARMAYEH(
        id = "SARMAYEH",
        nameFa = "بانک سرمایه",
        nameEn = "Sarmayeh Bank",
        colorHex = 0xFF4F46E5,
        senderKeywords = listOf("sarmayeh"),
        bodyKeywords = listOf("بانک سرمایه", "سرمایه", "sarmayeh")
    ),
    IRAN_ZAMIN(
        id = "IRAN_ZAMIN",
        nameFa = "بانک ایران زمین",
        nameEn = "Iran Zamin Bank",
        colorHex = 0xFF7E22CE,
        senderKeywords = listOf("iranzamin", "izbank"),
        bodyKeywords = listOf("ایران زمین", "ایران‌زمین", "iran zamin")
    ),
    POST_BANK(
        id = "POST_BANK",
        nameFa = "پست بانک ایران",
        nameEn = "Post Bank Iran",
        colorHex = 0xFF047857,
        senderKeywords = listOf("postbank"),
        bodyKeywords = listOf("پست بانک", "post bank")
    ),
    OTHER(
        id = "OTHER",
        nameFa = "سایر بانک‌ها",
        nameEn = "Other Bank",
        colorHex = 0xFF475569
    );

    fun displayName(language: AppLanguage): String =
        if (language == AppLanguage.FA) nameFa else nameEn

    companion object {
        fun fromId(id: String): IranianBank = entries.firstOrNull { it.id == id } ?: OTHER

        fun detect(sender: String, normalizedBody: String): IranianBank? {
            val cleanSender = sender.lowercase().replace("+", "").replace(" ", "").replace("-", "")
            val lowerBody = normalizedBody.lowercase()

            // Prefer body match for neo-banks like Blu / Wepod (which may share parent shortcodes)
            for (neo in listOf(BLU, WEPOD)) {
                if (neo.bodyKeywords.any { lowerBody.contains(it) }) return neo
            }

            // Body keyword match (most reliable when bank name is printed in SMS)
            for (bank in entries) {
                if (bank == OTHER) continue
                if (bank.bodyKeywords.any { lowerBody.contains(it) }) return bank
            }

            // Sender ID / shortcode match
            for (bank in entries) {
                if (bank == OTHER) continue
                if (bank.senderKeywords.any { kw -> cleanSender.contains(kw) }) return bank
            }

            return null
        }
    }
}
