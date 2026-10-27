# End-to-End Deployment & Monetization Guide

This guide takes you step-by-step through releasing **Any-Translate** to the **Google Play Store**, setting up **Google AdMob monetization**, and automating builds with **GitHub Actions**.

---

## 1. Prerequisites Checklist

* **Google Play Console Account**: A one-time \$25 registration fee at [play.google.com/console](https://play.google.com/console).
* **Google AdMob Account**: Free account at [admob.google.com](https://admob.google.com).
* **Android Studio**: Recommended version Jellyfish, Koala, or Ladybug.
* **JDK 17**: Default Java version used by Gradle 8+.

---

## 2. Generating Your Production Release Keystore

To upload your app to Google Play, you need a cryptographic signing key.

Open your terminal and run:

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias anytranslate
```

You will be prompted to:
1. Enter a secure keystore password.
2. Enter your name, organization, and location details.
3. Keep this file (`release-key.jks`) safe! **Never commit it to a public GitHub repository.**

---

## 3. Configuring Release Signing in Gradle

Add the signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "YOUR_KEYSTORE_PASSWORD"
            keyAlias = System.getenv("KEY_ALIAS") ?: "anytranslate"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "YOUR_KEY_PASSWORD"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

To build a production **Android App Bundle (.aab)**:

```bash
./gradlew bundleRelease
```

The resulting file will be located at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 4. Google AdMob Monetization Setup

Any-Translate is built with AdMob **Banner** and **Interstitial** ad integration.

### Step 4.1: Register on Google AdMob
1. Visit [admob.google.com](https://admob.google.com) and sign in.
2. Click **Apps** ➔ **Add App** ➔ Select **Android** ➔ Choose whether your app is already listed on Play Store (select No initially).
3. Copy your unique **AdMob App ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`).

### Step 4.2: Create Ad Units
1. **Banner Ad**:
   - Format: Banner
   - Name: `Main Screen Bottom Banner`
   - Copy the generated Ad Unit ID (format: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`).
2. **Interstitial Ad**:
   - Format: Interstitial
   - Name: `Lookup Completed Interstitial`
   - Copy the generated Ad Unit ID.

### Step 4.3: Swap Test IDs for Production IDs
In `app/src/main/AndroidManifest.xml`:
```xml
<!-- Replace test ID with your production AdMob App ID -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY" />
```

In `app/src/main/res/values/strings.xml`:
```xml
<!-- Replace with your live Ad Unit IDs -->
<string name="admob_banner_test_id">ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ</string>
<string name="admob_interstitial_test_id">ca-app-pub-XXXXXXXXXXXXXXXX/AAAAAAAAAA</string>
```

In `app/src/main/java/com/bhaskar/anytranslate/utils/AdMobHelper.kt`:
Update `TEST_INTERSTITIAL_AD_UNIT_ID` to point to your live unit ID or `@string/admob_interstitial_test_id`.

---

## 5. Google Play Store Console Submission

1. **Create Application**:
   - Go to Google Play Console.
   - Click **Create app**.
   - App name: `Any-Translate: English to German, Nepali & Hindi`
   - Default language: `English (United States)`
   - App or game: `App`
   - Free or paid: `Free`

2. **Store Listing Details**:
   - **Short description**: Instant translation of English into German, Nepali, and Hindi with offline dictionary.
   - **Full description**:
     > Any-Translate is an intelligent dictionary and translation application designed to bridge English with German, Nepali, and Hindi seamlessly.
     > 
     > Key Features:
     > - 100% Offline Dictionary: Instant lookups for words, grammatical definitions, and pronunciation guides.
     > - On-Device AI Powered: Powered by Google ML Kit translation engine for offline sentence and phrase processing.
     > - Simultaneous Multilingual Display: See German, Nepali, and Hindi results all at once or filter by your preferred language.
     > - Native Text-to-Speech: Listen to crystal-clear pronunciations in German and Hindi.
     > - Favorites & History: Bookmark essential words and access recent translations offline anytime.

3. **Graphic Assets**:
   - App Icon: 512 x 512 px PNG (32-bit with alpha).
   - Feature Graphic: 1024 x 500 px JPG/PNG.
   - Screenshots: Phone (at least 2, 16:9 or 18:9 aspect ratio).

4. **Data Safety & Policy Disclosures**:
   - **Ads**: Check "Yes, my app contains ads".
   - **Data Collection**: Disclose anonymous diagnostics/crash logs used by Google Mobile Ads SDK.
   - **Privacy Policy URL**: Host `docs/PRIVACY_POLICY.md` on GitHub Pages (e.g. `https://bhaskar-sah.github.io/Any-Translate/privacy.html`).

5. **Release Track**:
   - Navigate to **Production** (or **Internal Testing** first).
   - Upload `app-release.aab`.
   - Add release notes (e.g. `Initial release v1.0.0 with offline dictionary and on-device ML translation`).
   - Click **Save** and **Start rollout to Production**!

---

## 6. GitHub Actions CI/CD Integration

Every commit to `main` triggers `.github/workflows/android-build.yml`:
- Compiles the project using JDK 17 and Android SDK 34.
- Runs all unit tests.
- Packages `app-debug.apk` and uploads it to GitHub Actions Artifacts.
- When you push a git tag like `git tag v1.0.0 && git push origin v1.0.0`, GitHub Actions automatically publishes a GitHub Release with the APK attached!
