# Any-Translate 🌐

[![Android CI/CD](https://github.com/bhaskar-sah/Any-Translate/actions/workflows/android-build.yml/badge.svg)](https://github.com/bhaskar-sah/Any-Translate/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-green.svg)](https://developer.android.com)

> An offline-first, AI-powered universal multilingual Android dictionary and translation application that enables translating in **ANY** direction between **English (🇬🇧)**, **German (🇩🇪)**, **Nepali (🇳🇵)**, and **Hindi (🇮🇳)** with automatic language identification.

---

## 📱 App Highlights & Architecture

Any-Translate combines high-performance on-device neural machine translation with a rich, pre-loaded SQLite offline database to deliver instant, zero-cost translations.

```
                         ┌──────────────────────────────────────────────┐
                         │   Input (English, German, Nepali or Hindi)   │
                         └──────────────────────┬───────────────────────┘
                                                │
                                                ▼
                         ┌──────────────────────────────────────────────┐
                         │  Language Detection (Auto / Manual Selection) │
                         └──────────────────────┬───────────────────────┘
                                                │
                                                ▼
                         ┌──────────────────────────────────────────────┐
                         │         TranslationRepository (SSOT)         │
                         └──────────────────────┬───────────────────────┘
                                                │
           ┌────────────────────────────────────┼────────────────────────────────────┐
           ▼                                    ▼                                    ▼
┌─────────────────────────┐          ┌─────────────────────────┐          ┌─────────────────────────┐
│ Room Offline Database   │          │ Google ML Kit AI        │          │ Free Online Fallback    │
│ • Universal Lookups     │          │ • On-Device Neural MT   │          │ • Extended Queries      │
│ • Instant Definitions   │          │ • English ⇄ German      │          │ • Free, No API Keys     │
│ • Bilingual Examples    │          │ • English ⇄ Hindi       │          │ • Multi-pair coverage   │
└─────────────────────────┘          └─────────────────────────┘          └─────────────────────────┘
```

### Key Features
* 🔄 **Any-to-Any Universal Translation**: Type input in **English**, **German**, **Nepali**, or **Hindi**, and the app translates into all other 3 languages simultaneously.
* ✨ **On-Device AI Language Auto-Detection**: Uses Google ML Kit Language Identification and script detection to detect the input language in milliseconds.
* ⚡ **100% Offline SQLite / Room Dictionary**: Pre-loaded vocabulary database containing common words, parts of speech, phonetic transliterations, and contextual example sentences queryable in any language.
* 🧠 **On-Device Google ML Kit Neural Translation**: On-device neural machine translation models for German and Hindi.
* 🔊 **Native Text-To-Speech (TTS)**: Listen to audio pronunciations spoken aloud in English, German, and Hindi.
* 📋 **One-Tap Actions**: Copy translation to clipboard, share to WhatsApp/SMS, or star/bookmark to saved favorites.
* 📜 **Search History & Bookmarks**: Full offline persistence for recent lookups and favorited vocabulary.
* 💰 **AdMob Monetization Ready**: Integrated Google AdMob Banner and frequency-capped Interstitial ads using official test IDs.
* 🌐 **Companion Web Simulator**: Test the universal dictionary engine immediately in your browser via `web/index.html`.

---

## 🛠️ Tech Stack & Dependencies

* **Language**: Kotlin 1.9.24
* **UI**: Material Components 3 (Material Design), ViewBinding, ConstraintLayout
* **Architecture**: MVVM (Model-View-ViewModel) + Single Source of Truth Repository
* **Async & Concurrency**: Kotlin Coroutines & StateFlow / SharedFlow
* **Local Database**: Android Jetpack Room 2.6.1 + SQLite
* **On-Device Machine Learning**: Google ML Kit Translation (`com.google.mlkit:translate:17.0.3`)
* **Monetization**: Google Mobile Ads SDK (`play-services-ads:23.1.0`)
* **Networking**: Square OkHttp 4.12.0 & Google Gson 2.11.0
* **CI/CD**: GitHub Actions workflow for automatic unit tests and APK compilation

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/bhaskar-sah/Any-Translate.git
cd Any-Translate
```

### 2. Open in Android Studio
1. Open Android Studio (recommended: **Hedgehog**, **Iguana**, **Jellyfish**, **Koala**, or **Ladybug**).
2. Select **File > Open...** and choose the `Any-Translate` directory.
3. Allow Gradle to sync dependencies automatically.

### 3. Build & Run
- Connect an Android device with USB debugging enabled, or launch an Android Virtual Device (AVD).
- Press **Run 'app'** (`Shift + F10`) or execute from terminal:
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```
The compiled APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Companion Web Simulator

You can also run and preview the dictionary translation engine instantly in your browser:
```bash
cd web
python3 -m http.server 8080
```
Then open [http://localhost:8080](http://localhost:8080) in any web browser.

---

## 📦 Deployment to Google Play Store

For detailed, step-by-step instructions on publishing to Google Play and setting up live Google AdMob ads, check out the [Deployment Guide](docs/DEPLOYMENT_GUIDE.md).

Quick steps:
1. Generate your release keystore using `keytool`.
2. Replace AdMob test IDs in `AndroidManifest.xml` and `strings.xml` with your production IDs.
3. Build the Android App Bundle:
   ```bash
   ./gradlew bundleRelease
   ```
4. Upload `app/build/outputs/bundle/release/app-release.aab` to Google Play Console.
5. Provide the [Privacy Policy](docs/PRIVACY_POLICY.md) URL in your Play Console listing.

---

## ⚙️ GitHub Actions CI/CD

This repository includes a pre-configured GitHub Actions workflow located in `.github/workflows/android-build.yml`.

Whenever code is pushed to `main` or a pull request is opened, GitHub Actions:
1. Sets up JDK 17 (Zulu OpenJDK).
2. Sets up Android SDK 34.
3. Runs unit tests (`./gradlew test`).
4. Builds the debug and release APKs.
5. Uploads the generated APK as an artifact for download.
6. Automatically publishes a GitHub Release when a git tag (e.g. `v1.0.0`) is pushed.

---

## 👨‍💻 Author

* **Bhaskar Sah**
  * GitHub: [@bhaskar-sah](https://github.com/bhaskar-sah)
  * Email: [bhaskarsah1997@gmail.com](mailto:bhaskarsah1997@gmail.com)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
