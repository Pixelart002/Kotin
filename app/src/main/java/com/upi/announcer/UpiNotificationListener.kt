package com.upi.announcer

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class UpiNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech

    override fun onCreate() {
        super.onCreate()
        // Offline TTS initialize kar rahe hain
        tts = TextToSpeech(this, this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // Sirf UPI Payment apps ko filter karo (PhonePe Biz, GPay Biz, Paytm Biz)
        val targetApps = listOf("com.phonepe.app.business", "com.google.android.apps.nbu.paisa.merchant", "com.paytm.business")
        
        if (targetApps.contains(packageName)) {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: "" // Example: "Received ₹150"
            val text = extras.getString("android.text") ?: ""   // Example: "from Rahul"

            // Agar notification me Rupee symbol hai, toh announce karo
            if (title.contains("₹") || title.contains("Rs")) {
                val announcement = "$title $text"
                speakOut(announcement)
            }
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Indian English ya Hindi set karein jisse pronunciation sahi aaye
            tts.language = Locale("hi", "IN") 
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}