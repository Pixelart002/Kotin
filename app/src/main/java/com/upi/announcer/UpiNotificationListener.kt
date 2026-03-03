package com.upi.announcer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import java.util.Locale

class UpiNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false

    private val targetApps = setOf(
        "com.phonepe.app.business",
        "com.google.android.apps.nbu.paisa.merchant",
        "com.paytm.business",
        "net.one97.paytm",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "in.org.npci.upiapp",
        "com.naviapp"
    )

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        
        // 🚀 PRODUCTION FIX: Background mein zinda rehne ke liye ek notification dikhana zaruri hai
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "SERVICE_CHANNEL")
            .setContentTitle("UPI Announcer Active")
            .setContentText("Listening for payment notifications...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_low)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        
        startForeground(1, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        if (targetApps.contains(packageName)) {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val text = extras.getString("android.text") ?: ""
            val bigText = extras.getString("android.bigText") ?: ""

            val fullTextLower = "$title $text $bigText".lowercase()

            // Optimized Keywords
            val hasMoneySymbol = fullTextLower.contains("₹") || fullTextLower.contains("rs") || fullTextLower.contains("inr")
            val isReceived = fullTextLower.contains("received") || fullTextLower.contains("credited") || 
                             fullTextLower.contains("paid you") || fullTextLower.contains("sent you") ||
                             fullTextLower.contains("receive")
                             
            val isNotSent = !fullTextLower.contains("paid to") && !fullTextLower.contains("sent to") && 
                            !fullTextLower.contains("debited") && !fullTextLower.contains("cashback won")

            if (hasMoneySymbol && isReceived && isNotSent) {
                // Formatting for cleaner speech (₹ symbol ko "Rupees" bulwane ke liye)
                val cleanAnnouncement = "$title $text".replace("₹", " Rupees ")
                speakOut(cleanAnnouncement)
            }
        }
    }

    private fun speakOut(text: String) {
        if (isTtsReady) {
            // 🚀 Audio Focus logic: Announcement ke waqt baaki aawaz dhire ho jayegi
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