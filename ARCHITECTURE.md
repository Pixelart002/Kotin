# Architecture Overview
# UPI Voice Alert - Offline Notification Announcement System

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    User's Android Device                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Capacitor WebView                         │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │           Frontend UI (HTML/CSS/JS)            │ │ │
│  │  │  - Settings Panel                              │ │ │
│  │  │  - Transaction History                         │ │ │
│  │  │  - Permission Manager                          │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
│                           ▲                                  │
│                           │ Bridge                           │
│                           ▼                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │          Android Native Layer (Kotlin)                 │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │                                                         │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │  UPI Notification Listener Service              │  │ │
│  │  │  ┌───────────────────────────────────────────┐  │  │ │
│  │  │  │ Monitors: Google Pay, PhonePe, Paytm,    │  │  │ │
│  │  │  │          BHIM, Amazon Pay, etc.          │  │  │ │
│  │  │  └───────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  │                      ▼                                 │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │  UPI Notification Parser                        │  │ │
│  │  │  ┌───────────────────────────────────────────┐  │  │ │
│  │  │  │ Extract:                                  │  │  │ │
│  │  │  │ - Sender Name (Regex patterns)            │  │  │ │
│  │  │  │ - Amount (₹ symbols parsing)              │  │  │ │
│  │  │  │ - Transaction Type (sent/received)        │  │  │ │
│  │  │  └───────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  │                      ▼                                 │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │  TTS Engine (Text-to-Speech)                   │  │ │
│  │  │  ┌───────────────────────────────────────────┐  │  │ │
│  │  │  │ Format: "Received ₹5000 from Raj"        │  │  │ │
│  │  │  │ Language: English, Hindi, Tamil, Telugu  │  │  │ │
│  │  │  │ Output: Device Speaker Audio              │  │  │ │
│  │  │  └───────────────────────────────────────────┘  │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  │                      ▼                                 │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │  Android System Services                       │  │ │
│  │  │  ├─ AudioManager (Volume Control)              │  │ │
│  │  │  ├─ VibratorManager (Haptic Feedback)          │  │ │
│  │  │  └─ SharedPreferences (Data Persistence)       │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  │                                                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                          ▼
        ┌────────────────────────────────┐
        │    Device Speaker Output       │
        │   🔊 "Received ₹5000 from Raj" │
        └────────────────────────────────┘
```

## Data Flow

```
Notification Received (Android System)
        │
        ▼
NotificationListenerService onNotificationPosted()
        │
        ├─ Check if UPI app (package name matching)
        │
        ▼
Extract notification text
        │
        ├─ Title: "Verification successful"
        ├─ Text: "₹500 transferred to Raj"
        └─ SubText: "Google Pay"
        │
        ▼
UPINotificationParser
        │
        ├─ Pattern matching for name
        ├─ Regex for amount extraction
        └─ Transaction type identification
        │
        ▼
Parse Result Map
        │
        ├─ senderName: "Raj"
        ├─ amount: "rupees 500"
        └─ type: "transferred"
        │
        ▼
Generate Announcement String
        │
        └─ "Received rupees 500 from Raj"
        │
        ▼
TTSEngine.speak(text)
        │
        ├─ Load language locale
        ├─ Queue audio task
        ├─ Apply volume settings
        └─ Output to speaker
        │
        ▼
User Hears Announcement
```

## Component Responsibilities

### MainActivity (Entry Point)
- Initializes TTS engine on app start
- Cleans up TTS on app close
- Manages app lifecycle
- No UI handling (handled by Capacitor WebView)

### UPINotificationListenerService
- Listens to system notifications
- Filters by UPI app package names
- Calls parser for data extraction
- Triggers TTS announcement
- Handles errors gracefully

### UPINotificationParser
- Regex-based text pattern matching
- Extracts sender/receiver name
- Parses currency amounts
- Identifies transaction type
- Returns structured data map

### TTSEngine
- Initializes Android TextToSpeech API
- Manages language selection
- Queues speech tasks
- Handles state (ready/not-ready)
- Graceful shutdown

### Frontend (HTML/JS)
- Settings UI for user preferences
- Permission request handler
- Transaction history display
- Volume and language controls
- Data persistence (localStorage)

## Storage & Persistence

```
Device Storage:
├── SharedPreferences (Android Native)
│   └─ TTS language preference
│
├─ localStorage (Web, Capacitor)
│   ├─ User settings (volume, voice enabled)
│   ├─ AppState (online/offline)
│   └─ Recent transactions (max 20)
│
└─ System Audio
    └─ TTS cache (handled by OS)
```

## Offline-First Design

✅ **Fully Offline:**
- Notification listening (system-level)
- Parsing (local regex)
- Short text-to-speech (device's TTS engine)
- Settings storage (local)
- Transaction history (local)

❌ **Not Required:**
- Network connectivity
- Server/API calls
- Cloud storage
- Internet permissions (optional)

## Security Considerations

```
User Data:
├─ Notification content (NOT uploaded)
├─ Settings (Local only)
├─ Transaction history (Local only)
└─ No personal data collection

Permissions:
├─ BIND_NOTIFICATION_LISTENER_SERVICE ← Necessary
├─ RECORD_AUDIO ← For TTS engine
├─ MODIFY_AUDIO_SETTINGS ← Volume control
└─ INTERNET ← Optional (not used in v1.0)

Isolation:
├─ Service runs in background
├─ No broadcast receivers
├─ No content providers
└─ App-specific storage only
```

## Extendability Points

Future enhancements can add:

```kotlin
// 1. Custom announcement templates
interface AnnouncementTemplate {
    fun format(data: Map<String, String>): String
}

// 2. Multiple TTS engines
interface TTSProvider {
    fun speak(text: String, language: String)
}

// 3. Network sync (optional)
interface SyncProvider {
    fun syncTransactions(data: List<Transaction>)
}

// 4. Analytics (optional)
interface AnalyticsProvider {
    fun logEvent(event: String, data: Map<String, Any>)
}

// 5. Custom notification filters
interface NotificationFilter {
    fun matches(notification: StatusBarNotification): Boolean
}
```

---

**Architecture designed for:**
- Simplicity (minimal dependencies)
- Offline operation (no internet required)
- Performance (lightweight)
- Privacy (local storage only)
- Extensibility (pluggable components)

**Technology Stack:**
- Frontend: Vanilla JavaScript (no frameworks)
- Native: Pure Kotlin (no abstraction layers)
- Build: Gradle (standard Android)
- CI/CD: GitHub Actions (industry standard)
