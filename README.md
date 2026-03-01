# README and Setup Guide for UPI Voice Alert App

## 📱 Project Overview

UPI Voice Alert is a production-ready Android application that listens for UPI payment notifications and announces key details (sender name and amount) using Text-to-Speech (TTS) technology. The app operates fully offline and provides a soundbox-like announcement system.

### Features
- ✅ Real-time UPI notification monitoring
- ✅ Text-to-Speech announcements
- ✅ Automatic transaction tracking
- ✅ Volume and language controls
- ✅ Offline functionality
- ✅ Automatic APK generation via CI/CD

## 🔧 Setup Instructions

### Prerequisites
- Node.js 20+
- Java Development Kit (JDK) 17+
- Android SDK (API 26-34)
- Gradle 8.2+
- Git

### Local Development Setup

1. **Clone and install dependencies:**
   ```bash
   cd /workspaces/Kotin
   npm install
   npm install -g @capacitor/cli
   ```

2. **Sync Capacitor:**
   ```bash
   npx cap sync android
   ```

3. **Open Android Studio:**
   ```bash
   npx cap open android
   ```

4. **Build and run:**
   - Connect an Android device (API 26+)
   - Run: `gradle assembleDebug` or use Android Studio

### Production APK Generation

#### Option 1: Local Build
1. **Create keystore:**
   ```bash
   keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
   ```

2. **Build release APK:**
   ```bash
   export KEYSTORE_ALIAS=release
   export KEYSTORE_ALIAS_PASSWORD=your_alias_password
   export KEYSTORE_PASSWORD=your_keystore_password
   export KEYSTORE_PATH=keystore.jks
   
   cd android
   ./gradlew assembleRelease
   ```

   **APK Location:** `android/app/build/outputs/apk/release/app-release.apk`

#### Option 2: GitHub Actions CI/CD (Recommended)
1. **Setup GitHub Secrets:**
   - Go to: Settings → Secrets and variables → Actions
   - Add these secrets:
     - `KEYSTORE_CONTENT`: Base64-encoded keystore.jks
     - `KEYSTORE_ALIAS`: Your key alias
     - `KEYSTORE_ALIAS_PASSWORD`: Key password
     - `KEYSTORE_PASSWORD`: Keystore password

2. **Encode keystore for GitHub:**
   ```bash
   base64 keystore.jks | tr -d '\n' > keystore_base64.txt
   # Copy content of keystore_base64.txt to KEYSTORE_CONTENT secret
   ```

3. **Automatic builds on:**
   - Push to main branch
   - Tag release: `git tag v1.0.0 && git push --tags`
   - Manual trigger: GitHub Actions → Build & Release APK → Run workflow

4. **Download APK:**
   - Go to GitHub Actions workflow run
   - Download artifact: `upi-voice-alert-release`

## 🔐 Permissions Required

The app requires these permissions in `AndroidManifest.xml`:
- `BIND_NOTIFICATION_LISTENER_SERVICE` - To listen for notifications
- `RECORD_AUDIO` - For TTS engine
- `MODIFY_AUDIO_SETTINGS` - To control volume
- `INTERNET` - For API calls (if needed)

Users must manually grant:
1. **Notification Listener Access:**
   - Settings → Apps → Special app access → Notification listener service → Enable UPI Voice Alert

2. **Audio Permissions:**
   - Grant when app first runs

## 📦 APK Distribution

### Signed APK Details
- **Path:** `android/app/build/outputs/apk/release/app-release.apk`
- **Package Name:** `com.upivoicealert.app`
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Size:** ~15-20 MB (varies by dependencies)

### Installation Methods

**Method 1: Direct APK Installation**
```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

**Method 2: Google Play Store**
- Build AAB: `./gradlew bundleRelease`
- Output: `android/app/build/outputs/bundle/release/app-release.aab`
- Upload to Play Store Console

**Method 3: GitHub Releases**
- Tagged versions auto-create releases with APK and AAB
- View all releases: `https://github.com/Pixelart002/Kotin/releases`

## 🏗️ Project Structure

```
/workspaces/Kotin/
├── android/                           # Native Android code
│   ├── app/src/main/
│   │   ├── kotlin/com/upivoicealert/  # Kotlin source files
│   │   │   ├── MainActivity.kt        # Main activity entry point
│   │   │   ├── services/
│   │   │   │   └── UPINotificationListenerService.kt  # Notification listener
│   │   │   └── utils/
│   │   │       ├── TTSEngine.kt       # Text-to-Speech engine
│   │   │       └── UPINotificationParser.kt  # UPI data parsing
│   │   └── AndroidManifest.xml        # Permissions & services
│   ├── app/build.gradle.kts          # App-level config
│   ├── build.gradle.kts              # Project-level config
│   └── settings.gradle.kts           # Module settings
├── src/                              # Web assets
│   ├── index.html                    # Main UI
│   ├── styles.css                    # Styling
│   └── app.js                        # Frontend logic
├── .github/workflows/
│   └── build-release.yml             # CI/CD pipeline
├── capacitor.config.ts               # Capacitor configuration
├── package.json                      # Dependencies
└── tsconfig.json                     # TypeScript config
```

## 🔄 CI/CD Pipeline (GitHub Actions)

### Workflow: `build-release.yml`

**Triggers:**
- On push to `main` branch → Build release APK
- On pull requests → Build debug APK
- On version tags (`v*`) → Create GitHub release with APK

**Steps:**
1. Checkout code
2. Setup JDK 17
3. Install Node.js dependencies
4. Build web assets (`npm run build:prod`)
5. Sync Capacitor
6. Build APK (debug or release)
7. Upload artifacts
8. Create GitHub release (for tags)

### Manual Workflow Trigger
```bash
# Option 1: Push to main (builds release APK)
git commit -m "Feature: Add voice alert"
git push origin main

# Option 2: Create release tag
git tag v1.0.0
git push --tags

# Option 3: Manual trigger in Actions tab
# Go to: Actions → Build & Release APK → Run workflow
```

## 🛠️ Development Commands

```bash
# Install dependencies
npm install

# Build web assets for production
npm run build:prod

# Sync Capacitor with Android
npx cap sync android

# Open Android Studio
npx cap open android

# Build debug APK locally
cd android && ./gradlew assembleDebug

# Build release APK locally
cd android && ./gradlew assembleRelease

# Run tests
cd android && ./gradlew test

# Clean build
cd android && ./gradlew clean
```

## 🧪 Testing

### Local Testing
1. Build APK and install on device
2. Grant required permissions
3. Open app and check Settings
4. Enable "Notification Listener Access"
5. Use test UPI app or make real transaction
6. Verify announcement plays

### Debug Logging
```bash
# Monitor logs
adb logcat | grep "UPI\|TTS"

# Full logs with timestamps
adb logcat -v time | grep "UPI"
```

## 📝 Supported UPI Apps

The app listens to notifications from:
- Google Pay (com.google.android.apps.nbu.paisa.user)
- PhonePe (com.phonepe.app)
- Paytm (com.paytm.android)
- BHIM (in.org.npci.upiapp)
- Amazon Pay (com.amazon.mShop.android.shopping)
- WhatsApp Payments (com.whatsapp)
- MobiKwik (com.mobikwik_new)

## 🚀 Deployment Checklist

- [ ] All permissions properly configured in manifest
- [ ] TTS engine tested on multiple devices
- [ ] Notification parsing verified with real UPI apps
- [ ] APK signed with release keystore
- [ ] Version number updated in `build.gradle.kts`
- [ ] GitHub secrets configured for CI/CD
- [ ] Release tag created and pushed
- [ ] APK downloaded from GitHub Releases
- [ ] Final APK tested on target devices

## 📄 License

This project is open source. See LICENSE file for details.

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

## ❓ FAQ

**Q: Why does the app need notification listener access?**
A: This permission is required to intercept UPI payment notifications from other apps.

**Q: Is the app completely offline?**
A: Yes, the notification parsing and TTS announcement work entirely offline using Android's built-in TextToSpeech API.

**Q: Can I customize the announcements?**
A: Yes, edit the `generateAnnouncement()` function in `UPINotificationListenerService.kt` to customize the announcement format.

**Q: How often is the APK built?**
A: On every push to main branch or tag. Development APKs are built on PRs and kept for 7 days. Release APKs are kept for 30 days.

---

**Version:** 1.0.0  
**Last Updated:** March 1, 2026  
**Status:** ✅ Production Ready
