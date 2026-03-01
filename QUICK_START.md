# UPI Voice Alert - Quick Start Guide

## 🚀 Quick Setup (5 Minutes)

### Option 1: Automated Setup Script
```bash
cd /workspaces/Kotin
bash setup.sh
# Follow the prompts to generate keystore and configure secrets
```

### Option 2: Manual Setup

**1. Install Dependencies**
```bash
npm install
npm install -g @capacitor/cli
```

**2. Build Web Assets**
```bash
npm run build:prod
```

**3. Sync Capacitor**
```bash
npx cap sync android
```

**4. Open in Android Studio**
```bash
npx cap open android
```

**5. Build & Run**
- Connect Android device (API 26+)
- Click "Run" in Android Studio
- Or use: `cd android && ./gradlew installDebug`

## 📦 Building Production APK

### Direct Build Method
```bash
# Create keystore first
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release

# Build
export KEYSTORE_ALIAS=release
export KEYSTORE_PASSWORD=your_password
export KEYSTORE_ALIAS_PASSWORD=your_password
export KEYSTORE_PATH=keystore.jks

bash build.sh
```

**Output:** `android/app/build/outputs/apk/release/app-release.apk`

### GitHub Actions (Automatic)

**Once per project:**
1. Encode keystore:
   ```bash
   base64 keystore.jks | tr -d '\n' > keystore_b64.txt
   cat keystore_b64.txt  # Copy this
   ```

2. Go to: https://github.com/Pixelart002/Kotin/settings/secrets/actions
3. Add these secrets:
   - `KEYSTORE_CONTENT` = (paste base64 content)
   - `KEYSTORE_ALIAS` = `release`
   - `KEYSTORE_PASSWORD` = (your password)
   - `KEYSTORE_ALIAS_PASSWORD` = (your password)

**Then automatically on:**
```bash
# Build release APK
git push origin main

# Build and create release
git tag v1.0.0
git push --tags
```

Download from: Actions → Artifacts

## 🔧 Useful Commands

```bash
# Web development
npm run dev              # Development server
npm run build:prod       # Production build

# Android development
npx cap sync android     # Sync changes
npx cap open android     # Open in Android Studio

# Building APK
cd android
./gradlew assembleDebug  # Debug APK
./gradlew assembleDebug --info  # With detailed logs

# Testing
adb logcat | grep "UPI"  # View logs
adb install app-release.apk  # Install APK

# Clean
cd android && ./gradlew clean
```

## ⚙️ Configuration

### Update App Version
Edit `android/app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode 2        // Increment by 1
    versionName "1.0.1"  // Semantic versioning
}
```

### Customize Announcements
Edit `android/app/src/main/kotlin/com/upivoicealert/services/UPINotificationListenerService.kt`:
```kotlin
private fun generateAnnouncement(upiData: Map<String, String>): String {
    val senderName = upiData["senderName"] ?: "Unknown"
    val amount = upiData["amount"] ?: "Unknown"
    return "Received $amount from $senderName"  // Customize here
}
```

### Add More UPI Apps
Edit `UPINotificationListenerService.kt`:
```kotlin
private fun isUPIApp(packageName: String): Boolean {
    val upiApps = listOf(
        "com.google.android.apps.nbu.paisa.user",
        "your.custom.app"  // Add here
    )
}
```

## 🧪 Testing Checklist

- [ ] Grant Notification Listener Access
- [ ] Test with real UPI transaction
- [ ] Sound plays correctly
- [ ] Vibration works (if enabled)
- [ ] Settings save properly
- [ ] APK installs without errors
- [ ] No crashes in logs

## 📲 Install on Device

### Via ADB
```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

### Via File Transfer
- Connect via USB
- Copy APK to device
- Open file manager and install

### Via APK Distribution
- Generate APK
- Share via email/cloud
- Recipients can install directly

## 🐛 Troubleshooting

**App crashes on startup:**
```bash
adb logcat -c
adb logcat | grep "UPI\|FATAL"
```

**No notifications received:**
- Go to Settings → Apps → Permissions → Notification listener service
- Enable the app

**TTS not working:**
- Check Settings → Accessibility → Text-to-speech
- Install Google Text-to-Speech if missing

**Build failures:**
```bash
cd android
./gradlew clean
./gradlew assembleRelease --stacktrace
```

## 📚 Learn More

- [Capacitor Documentation](https://capacitorjs.com)
- [Android Notification Listener](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Android Text-to-Speech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)

## 📞 Support

For issues, check:
1. README.md - Full documentation
2. GitHub Issues - Known issues
3. Logs - `adb logcat | grep UPI`

---

**Ready?** Run: `bash setup.sh`
