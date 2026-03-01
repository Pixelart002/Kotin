# Project-specific ProGuard rules
-keep class com.upivoicealert.** { *; }
-keep class com.getcapacitor.** { *; }

# Keep TTS classes
-keep class android.speech.tts.** { *; }

# Keep notification classes
-keep class android.service.notification.** { *; }

# Keep reflection for TTS
-keepclassmembers class * {
    public <methods>;
    public <fields>;
}

# Optimize
-optimizationpasses 5
-dontusemixedcaseclassnames
