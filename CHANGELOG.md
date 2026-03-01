# Changelog

All notable changes to UPI Voice Alert will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-03-01

### Added
- Initial release of UPI Voice Alert
- Text-to-Speech announcements for UPI transactions
- Support for major UPI apps: Google Pay, PhonePe, Paytm, BHIM, Amazon Pay, WhatsApp Pay, MobiKwik
- Real-time notification listener service
- Offline-first architecture with local TTS
- Customizable voice settings (volume, language)
- Vibration feedback option
- Transaction history tracking
- Notification permission management
- GitHub Actions CI/CD pipeline for automatic APK builds
- Signed release APK generation
- App Bundle (AAB) support for Google Play Store
- Comprehensive documentation and setup guides
- Material Design 3 UI
- Android 8.0+ (API 26+) support

### Features
- **Notification Listener**: Monitors UPI payment notifications in real-time
- **TTS Engine**: Offline text-to-speech using Android's built-in engine
- **Parser**: Intelligent extraction of sender name and transaction amount
- **Voice Customization**: Adjust volume, language, and enable/disable announcements
- **Settings Persistence**: Saves user preferences locally
- **Transaction Log**: View recent transactions in the app
- **Permission Management**: Guide users through required permission setup

### Technical Details
- Built with Capacitor 6.0
- Kotlin for Android native code
- TypeScript/JavaScript frontend
- Gradle-based build system
- Production-ready signing configuration
- Automated testing and builds via GitHub Actions

### Known Limitations
- Android only (v8.0+)
- Requires Notification Listener permission
- Depends on active notification from UPI app
- TTS quality depends on device's language pack

---

## Unreleased (Next Release)

### Planned Features
- [ ] Custom announcement templates
- [ ] Multi-language support enhancement
- [ ] Notification sound customization
- [ ] Transaction filtering options
- [ ] Export transaction history
- [ ] Dark mode support
- [ ] iOS support (pending Capacitor improvements)
- [ ] Cloud sync for settings
- [ ] Notification retry logic
- [ ] Advanced parsing for more UPI apps

### Improvements
- [ ] Performance optimization
- [ ] Better error handling
- [ ] Enhanced logging for debugging
- [ ] Unit test coverage
- [ ] Integration tests

---

## Version History

### [0.0.1] - Development
- Project scaffold
- Initial architecture design
- Core component implementation
