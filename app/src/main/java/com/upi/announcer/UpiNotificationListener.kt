package com.upi.announcer

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

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, "SERVICE_CHANNEL")
            .setContentTitle("UPI Announcer Active")
            .setContentText("Smartly extracting amount and name...")
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

        // Maharaj Singh pattern ke liye puri text combine kari
        val fullText = "$title $text $bigText".replace("\n", " ")
        val fullTextLower = fullText.lowercase()

        // Money Received check
        val isReceived = fullTextLower.contains("received") || 
                         fullTextLower.contains("credited") || 
                         fullTextLower.contains("paid you") ||
                         fullTextLower.contains("has sent") // 🚀 PhonePe Fix

        val isNotDebit = !fullTextLower.contains("paid to") && 
                         !fullTextLower.contains("sent to")

        if (isReceived && isNotDebit) {
            // Amount Regex
            val amountRegex = Regex("""(?:₹|Rs\.?|INR)\s*(\d+(?:,\d{3})*(?:\.\d{1,2})?)""")
            
            // Name Regex: Handles "Maharaj Singh has sent" OR "from Rahul"
            val nameRegex = Regex("""(?:(.*)\s+has sent|(?:from|sent by)\s+([^,.\n]+))""", RegexOption.IGNORE_CASE)

            val amountMatch = amountRegex.find(fullText)
            val nameMatch = nameRegex.find(fullText)

            val amount = amountMatch?.groupValues?.get(1) ?: ""
            
            // Name extraction logic
            var senderName = nameMatch?.groupValues?.get(1)?.trim() 
                             ?: nameMatch?.groupValues?.get(2)?.trim() 
                             ?: "Customer"
            
            if (senderName.lowercase().contains("money received")) {
                senderName = senderName.replace("Money received", "", ignoreCase = true).trim()
            }
            if (senderName.isBlank()) senderName = "Customer"

            if (amount.isNotEmpty()) {
                val finalAnnouncement = "Money received $amount Rupees from $senderName"
                speakOut(finalAnnouncement)
            }
        }
    }

    private fun speakOut(text: String) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "UpiTask")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
            isTtsReady = true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SERVICE_CHANNEL", "UPI Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}