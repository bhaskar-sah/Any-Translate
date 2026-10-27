# Any-Translate 🌐

[![Android CI/CD](https://github.com/bhaskar-sah/Any-Translate/actions/workflows/android-build.yml/badge.svg)](https://github.com/bhaskar-sah/Any-Translate/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2024%2B)-green.svg)](https://developer.android.com)

> An offline-first, AI-powered multilingual Android dictionary and translation application that translates English words and phrases into **German (🇩🇪)**, **Nepali (🇳🇵)**, and **Hindi (🇮🇳)** simultaneously.

---

## 📱 App Highlights & Architecture

Any-Translate combines high-performance on-device neural machine translation with a rich, pre-loaded SQLite offline database to deliver instant, zero-cost translations.

```
                              ┌───────────────────────┐
                              │  User Query (English)  │
                              └───────────┬───────────┘
                                          │
                                          ▼
                         ┌─────────────────────────────────┐
                         │   TranslationRepository (SSOT)  │
                         └──────────────┬──────────────────┘
                                        │
           ┌────────────────────────────┼───────────────────────────┐
           ▼                            ▼                           ▼
┌───────────────────────┐    ┌───────────────────────┐    ┌───────────────────────┐
│ Room Offline Database │    │ Google ML Kit AI      │    │ Online Fallback API   │
│ - 100% Offline        │    │ - On-Device Neural MT │    │ - Extended Nepali     │
│ - Instant Definitions │    │ - English ➔ German    │    │ - Novel Full Phrases  │
│ - Transliterations    │    │ - English ➔ Hindi     │    │ - Free, No API Keys   │
│ - Example Sentences   │    │ - Zero Cloud Costs    │    │                       │
└───────────────────────┘    └───────────────────────┘    └───────────────────────┘
```

### Key Features
* 🌍 **Simultaneous Tri-Lingual Output**: Displays German, Nepali (Devanagari script), and Hindi (Devanagari script) results in dedicated Material 3 cards side-by-side or scrollable.
* ⚡ **100% Offline SQLite / Room Dictionary**: Pre-loaded vocabulary database containing common English words, parts of speech, phonetic transliterations, and contextual example sentences.
* 🧠 **On-Device Google ML Kit Neural Translation**: Automatic download and local execution of neural machine translation models for German and Hindi.
* 🔊 **Native Text-To-Speech (TTS)**: Hear audio pronunciations spoken aloud in German and Hindi.
* 📋 **One-Tap Actions**: Copy translation to clipboard, share to WhatsApp/SMS, or star/bookmark to saved favorites.
* 📜 **Search History & Bookmarks**: Full offline persistence for recent lookups and favorited vocabulary.
* 💰 **AdMob Monetization Ready**: Integrated Google AdMob Banner and frequency-capped Interstitial ads using official test IDs, ready to swap to live IDs for Google Play Store deployment.
* 🌐 **Companion Web Simulator**: Test the dictionary engine immediately in your browser via `web/index.html`.

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
