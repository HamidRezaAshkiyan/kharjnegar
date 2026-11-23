# Changelog

## v1.0.0

### Highlights
- **100% Offline & Open-Source**: Zero `INTERNET` or `ACCESS_NETWORK_STATE` permissions — enforced automatically in CI via `aapt2 dump permissions`.
- **25+ Iranian Banks & Neo-Banks Supported**: Mellat, Melli, Blu Bank, Saman, Saderat, Tejarat, Sepah, Pasargad, Wepod, Keshavarzi, Refah, Maskan, Parsian, Shahr, Ayandeh, Resalat, Mehr Iran, Sina, Eghtesad Novin, Karafarin, Dey, Gardeshgari, Sarmayeh, Iran Zamin, Post Bank, plus generic fallback.
- **Smart SMS Parser**:
  - Automatic OTP / dynamic password (`رمز پویا`) and failed transaction filtering
  - Rial (`ریال`) & Toman (`تومان`) auto-detection and normalization
  - Persian & Arabic digit normalization (`۰-۹`, `٠-٩`)
  - SHA-256 deduplication so re-scanning never duplicates entries
- **Solar Hijri (Jalali / Shamsi) Calendar**: Full Persian month navigation, daily spending chart, and 6-month income vs. expense comparison.
- **Bilingual UI (Persian RTL & English LTR)** with bundled offline Vazirmatn font and Material 3 Light/Dark themes.
- **Local Backup & Export**: UTF-8 BOM CSV export (Excel-ready for Persian) and full JSON backup/restore via Android Storage Access Framework.
