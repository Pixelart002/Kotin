# Production Deployment Guide

## Pre-Release Checklist

### Code Quality
- [ ] All tests passing
- [ ] No console errors in logs
- [ ] Code reviewed
- [ ] No hardcoded credentials
- [ ] Error handling implemented

### Android Configuration
- [ ] Version code incremented in build.gradle.kts
- [ ] proguard-rules.pro configured
- [ ] MinSdkVersion (26) appropriate
- [ ] TargetSdkVersion (34) up to date
- [ ] All permissions documented

### Release Preparation
- [ ] Keystore created and secured
- [ ] GitHub secrets configured
- [ ] README updated with changes
- [ ] CHANGELOG created
- [ ] Version tag planned

### Testing
- [ ] Debug APK tested on device
- [ ] All UPI apps tested
- [ ] TTS audio verified
- [ ] Notifications captured correctly
- [ ] Volume controls working
- [ ] Permissions granted properly

## Step-by-Step Release Process

### 1. Pre-Release Build

```bash
# Update version
vi android/app/build.gradle.kts
# Change versionCode += 1, versionName = "x.y.z"

# Test build
npm run build:prod
npx cap sync android
cd android
./gradlew assembleDebug
# Install and test on multiple devices
```

### 2. Release Build

```bash
# Build release APK
export KEYSTORE_ALIAS=release
export KEYSTORE_PASSWORD=****
export KEYSTORE_ALIAS_PASSWORD=****
export KEYSTORE_PATH=keystore.jks

cd android
./gradlew assembleRelease bundleRelease

# Verify output
ls -lh app/build/outputs/
```

### 3. Create Release Tag

```bash
# Commit changes
git add -A
git commit -m "Release: v1.0.1 - Add feature XYZ"

# Create annotated tag
git tag -a v1.0.1 -m "Version 1.0.1: Feature release"

# Push to GitHub
git push origin main --tags
```

GitHub Actions will automatically:
- Build release APK
- Build App Bundle (AAB)
- Create GitHub Release with artifacts
- Make APK available for download

### 4. Distribute APK

#### Direct Distribution
- Download from GitHub Releases
- Share `app-release.apk` via email/cloud
- Users can install directly

#### Google Play Store
- Download `app-release.aab`
- Upload to Play Store Console
- Wait for review (24-48 hours)
- Publish in phases (10% → 50% → 100%)

#### Enterprise Distribution
- Host APK on internal server
- Use MDM for device enrollment
- Auto-update via APK push

## Release Version Management

### Semantic Versioning Format
Version format: `MAJOR.MINOR.PATCH` (e.g., 1.0.0)

- **MAJOR**: Breaking changes (UI overhaul, new features)
- **MINOR**: New features (add new UPI app support)
- **PATCH**: Bug fixes (crash fixes, parser improvements)

### Version Code vs Version Name

```kotlin
// Version Code: Internal auto-incremented number
versionCode 5              // Always increment by 1

// Version Name: User-facing version string
versionName "1.0.5"        // Semantic versioning for users
```

## Post-Release Tasks

### 1. Monitor Performance
```bash
# Check crash reports in Google Play Console
# Monitor user feedback in reviews
# Track installation metrics
```

### 2. Update Documentation
```markdown
- Update CHANGELOG.md
- Update version in README.md
- Update installation URLs
- Document new features
```

### 3. Create Release Notes
```markdown
## Version 1.0.1
- Fixed notification parsing for PhonePe
- Added support for WhatsApp Payments
- Improved TTS performance
- Reduced app size by 15%
```

### 4. Monitor Issues
- Check GitHub Issues
- Monitor crash analytics
- Collect user feedback
- Plan next release

## Rollback Procedure

If critical issues are found:

```bash
# Revert to previous release
git checkout v1.0.0

# Build with previous version
bash build.sh

# Publish as hotfix
git tag v1.0.1-hotfix
git push --tags
```

## Security Considerations

### Keystore Management
✅ **DO:**
- Store keystore.jks in secure location
- Use strong passwords (16+ chars)
- Backup keystore file securely
- Rotate passwords annually
- Use separate keystore per app

❌ **DON'T:**
- Commit keystore to repository
- Share keystore or passwords
- Use same keystore across apps
- Upload keystore to GitHub
- Hardcode passwords in code

### GitHub Secrets
✅ **DO:**
- Only admins modify secrets
- Enable secret scanning
- Use organization-level secrets
- Rotate sensitive values
- Audit secret access

❌ **DON'T:**
- Log secrets in CI/CD output
- Share secrets via Slack/email
- Use repo secrets for sensitive data
- Store backup passwords in code

## CI/CD Pipeline Details

### Build Triggers

| Event | Action |
|-------|--------|
| Push to `main` | Build release APK |
| Pull Request | Build debug APK |
| Tag `v*` | Create GitHub Release |
| Manual trigger | Build specified variant |

### Build Steps
1. Checkout code
2. Setup JDK 17
3. Install Node dependencies
4. Build web assets
5. Sync Capacitor
6. Build APK (signed if release)
7. Upload artifacts
8. Create release (if tagged)

### Artifact Retention
- Debug APK: 7 days
- Release APK: 30 days
- Test reports: 7 days

## Performance Optimization

### APK Size Reduction
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
    }
}
```

### ProGuard Configuration
- Keep app classes
- Keep critical libraries
- Remove unused code
- Optimize bytecode

Current size: ~15-18 MB

## Monitoring & Analytics

### Built-in Monitoring
- Check logs: `adb logcat`
- Monitor performance in Android Studio
- Use Android Profiler for memory usage
- Check battery consumption

### User Analytics (Optional)
- Integrate Firebase (optional)
- Track app crashes
- Monitor feature usage
- Collect user feedback

## Support & Rollout Strategy

### Initial Release (v1.0.0)
- Limited rollout to testers
- Monitor for critical issues
- Collect feedback

### Staged Rollout
- Start with 10% of users
- Monitor crash rates
- Expand to 50%
- If stable, expand to 100%

### Emergency Hotfix
- Build immediately
- Tag as `v1.0.1-hotfix`
- Fast-track to users
- Document issue and fix

## Update Cycle

Recommended release schedule:
- **Major release**: Quarterly (Q1, Q2, Q3, Q4)
- **Minor release**: Monthly
- **Patch release**: As needed (bug fixes)
- **Hotfix**: Immediate (critical issues)

---

## Summary

**Release Flow:**
```
Code → Test → Build → Sign → Tag → Publish → Monitor → Support
```

**Key Files:**
- `android/app/build.gradle.kts` - Version management
- `.github/workflows/build-release.yml` - CI/CD automation
- `keystore.jks` - Release signing key
- `CHANGELOG.md` - Release notes

**Next Release Planned:** [Date]

---

**Last Updated:** March 1, 2026
