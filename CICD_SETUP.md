# CI/CD Pipeline Setup Guide

This document explains the CI/CD pipelines configured for the UPI Announcer Android App.

## Overview

Three GitHub Actions workflows have been created to automate the build, test, and deployment process:

1. **android-build.yml** - Main build pipeline (runs on push/PR)
2. **test.yml** - Comprehensive testing pipeline
3. **release.yml** - Release and Play Store deployment

## Workflows

### 1. Android Build & Test Pipeline (`android-build.yml`)

**Triggers:** Push to `main` or `develop`, Pull Requests

**Jobs:**
- **Lint Check**: Runs ESLint to validate code quality
- **Unit Tests**: Executes test suite and generates coverage reports
- **Build Android App**: 
  - Builds debug APK for testing
  - Builds release AAB for Play Store
- **Sign & Optimize APK**: Signs APK with production keys (main branch only)

**Artifacts Generated:**
- `app-debug.apk` - Debug APK for testing
- `app-release.aab` - Release Android App Bundle
- `app-signed` - Signed APK (if configured)

### 2. Automated Tests Pipeline (`test.yml`)

**Triggers:** Push to `main` or `develop`, Pull Requests, Daily at 2 AM UTC

**Jobs:**
- **Unit Tests**: Tests across multiple Node.js versions (16.x, 18.x, 20.x)
- **Code Quality**: ESLint and Prettier formatting checks
- **Security & Dependency Check**: Npm audit and Snyk vulnerability scanning

### 3. Release & Deploy Pipeline (`release.yml`)

**Triggers:** When a GitHub release is published

**Jobs:**
- **Deploy to Play Store**:
  - Builds production AAB with signing
  - Uploads to Google Play Store (internal track)
  - Generates and uploads release notes

## Setup Instructions

### Step 1: Configure GitHub Secrets

Add the following secrets to your GitHub repository:

#### For Signing APKs:

1. **SIGNING_KEY** (Base64 encoded keystore)
   ```bash
   # Generate if you don't have one:
   keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias upi-announcer
   
   # Encode to Base64:
   base64 -w 0 release.keystore > keystore.b64
   ```
   - Copy the contents of `keystore.b64` and add as `SIGNING_KEY` secret

2. **SIGNING_KEY_ALIAS** - Alias used in keystore (e.g., `upi-announcer`)
3. **SIGNING_KEY_PASSWORD** - Password for the key
4. **SIGNING_KEY_STORE_PASSWORD** - Password for the keystore

#### For Play Store Deployment:

1. **PLAY_STORE_SERVICE_ACCOUNT** (JSON)
   - Get from Google Play Console > Settings > API access > Create service account
   - Add the JSON content as this secret

2. **SNYK_TOKEN** (Optional, for vulnerability scanning)
   - Get from https://snyk.io/

### Step 2: Update Package Name

In `release.yml`, update the package name:
```yaml
packageName: com.example.upiannouncer  # Change to your actual package name
```

Check your Android manifest for the correct package name:
```bash
grep 'package=' android/app/src/main/AndroidManifest.xml
```

### Step 3: Android Build Configuration

Ensure your `android/build.gradle` or `android/gradle.properties` has:
```gradle
android {
    compileSdkVersion 34
    minSdkVersion 24  // or your minimum API level
    targetSdkVersion 34
    
    // For signing in CI/CD
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias System.getenv("SIGNING_KEY_ALIAS")
            keyPassword System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

### Step 4: Environment Setup

The pipelines use:
- **Java 17** (Temurin distribution)
- **Node.js 18**
- **Android SDK 34**
- **Gradle** (with caching)
- **Ubuntu Latest** runner

## Pipeline Behavior

### On Every Push/PR:
1. Code is linted
2. Tests are run
3. Debug APK and Release AAB are built
4. Artifacts are available for download

### On Main Branch Push:
- Same as above, plus:
- APK is signed (if secrets configured)

### On Release Creation:
- Production AAB is built
- Uploaded to Google Play Store internal testing track
- Release notes are generated

## Local Development

### Build Locally:
```bash
# Install dependencies
npm install

# Build web assets
npm run build

# Sync with Capacitor
npx cap sync android

# Build APK
cd android
./gradlew assembleDebug

# Build AAB
./gradlew bundleRelease
```

### Run Tests:
```bash
npm run test
npm run test:coverage
```

### Lint Code:
```bash
npm run lint
npm run lint:fix
npm run format
```

## Troubleshooting

### Build Fails with "Could not determine Java version"
- Ensure Java 17 is available
- Check `JAVA_HOME` environment variable

### Gradle Build Fails
- Clear Gradle cache: `cd android && ./gradlew clean`
- Update Android SDK: Run action with latest API level

### Tests Fail in CI but Pass Locally
- Check Node version differences
- Verify environment variables are set
- Check for platform-specific issues

### Play Store Upload Fails
- Verify service account JSON is valid
- Ensure app version is higher than previous release
- Check package name matches Play Store listing

## Monitoring & Validation

Check the status of your pipelines:
1. Go to your GitHub repository
2. Click on "Actions" tab
3. View workflow runs and logs
4. Download artifacts from successful builds

## Next Steps

1. Create your keystore and add signing secrets
2. Set up Google Play Console service account (if deploying)
3. Merge these workflow files to your main branch
4. Test with a push or PR
5. Monitor the Actions tab for results

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android Gradle Build System](https://developer.android.com/build)
- [Google Play Console API](https://developer.android.com/google-play/console)
- [Capacitor Android Documentation](https://capacitorjs.com/docs/android)
