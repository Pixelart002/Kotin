package com.upi.announcer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale
import java.util.regex.Pattern

class UpiNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "SERVICE_CHANNEL")
            .setContentTitle("UPI Announcer Active")
            .setContentText("Listening for all payment apps...")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        
        startForeground(1, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val bigText = extras.getString("android.bigText") ?: ""

        val fullText = "$title $text $bigText"
        val fullTextLower = fullText.lowercase()

        // 🚀 ADVANCED FILTER 1: Currency Detection
        // Sirf un notifications ko pakdega jinme Amount ki baat ho rahi hai
        val hasMoney = fullTextLower.contains("₹") || 
                       fullTextLower.contains("rs") || 
                       fullTextLower.contains("inr") ||
                       fullTextLower.contains("rupees")

        // 🚀 ADVANCED FILTER 2: Positive Keywords (Money Received)
        val isReceived = fullTextLower.contains("received") || 
                         fullTextLower.contains("credited") || 
                         fullTextLower.contains("paid you") || 
                         fullTextLower.contains("sent you") ||
                         fullTextLower.contains("receive") ||
                         fullTextLower.contains("success") && fullTextLower.contains("payment")

        // 🚀 ADVANCED FILTER 3: Negative Keywords (Spam/Debit rokne ke liye)
        // Isse "Paid to", "Sent to", ya "OTP" wali notifications filter ho jayengi
        val isNotSpamOrDebit = !fullTextLower.contains("paid to") && 
                               !fullTextLower.contains("sent to") && 
                               !fullTextLower.contains("debited") && 
                               !fullTextLower.contains("spent") &&
                               !fullTextLower.contains("otp") &&
                               !fullTextLower.contains("cashback won") &&
                               !fullTextLower.contains("recharge")

        if (hasMoney && isReceived && isNotSpamOrDebit) {
            // 🚀 ADVANCED CLEANING: Announcement ko natural banane ke liye
            // "₹100" ko "100 Rupees" bolne ke liye formatting
            val cleanText = formatAmountForSpeech(title, text)
            speakOut(cleanText)
        }
    }

    private fun formatAmountForSpeech(title: String, text: String): String {
        // Amount aur Sender Name ko saaf karne ke liye logic
        var announcement = "$title $text"
        
        // Symbols ko words me badalna taaki TTS sahi se bole
        announcement = announcement.replace("₹", " Rupees ")
        announcement = announcement.replace("Rs.", " Rupees ")
        announcement = announcement.replace("INR", " Rupees ")
        
        // Faltu characters hatana
        return announcement.replace(Regex("[^a-zA-Z0-9. ₹]"), " ")
    }

    private fun speakOut(text: String) {
        if (isTtsReady) {
            // QUEUE_ADD ensures multiple payments don't overlap
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "UpiTask")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            if (result != TextToSpeech.LANG_MISSING_DATA) {
                isTtsReady = true
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "SERVICE_CHANNEL",
                "UPI Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}