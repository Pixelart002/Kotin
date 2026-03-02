package com.upi.announcer

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class UpiNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech

    // 🚀 UPGRADE 1: Business ke saath Normal Apps ki Package IDs
    private val targetApps = setOf(
        "com.phonepe.app.business",               // PhonePe Business
        "com.google.android.apps.nbu.paisa.merchant", // GPay Business
        "com.paytm.business",                     // Paytm Business
        "net.one97.paytm",                        // Paytm (Normal)
        "com.phonepe.app",                        // PhonePe (Normal)
        "com.google.android.apps.nbu.paisa.user", // GPay (Normal)
        "in.org.npci.upiapp"                      // BHIM UPI (Normal)
    )

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        if (targetApps.contains(packageName)) {
            val extras = sbn.notification.extras
            
            // Notification ka title aur text nikal rahe hain
            val title = extras.getString("android.title") ?: ""
            val text = extras.getString("android.text") ?: ""
            val bigText = extras.getString("android.bigText") ?: "" // Kabhi kabhi lamba message isme chhupa hota hai

            val fullTextLower = "$title $text $bigText".lowercase()

            // 🚀 UPGRADE 2: Smart Filter Logic (Sirf received money pakdega)
            val hasMoneySymbol = fullTextLower.contains("₹") || fullTextLower.contains("rs") || fullTextLower.contains("inr")
            
            // Yeh words hone chahiye (Paise aane ke keywords)
            val isReceived = fullTextLower.contains("received") || fullTextLower.contains("credited") || 
                             fullTextLower.contains("paid you") || fullTextLower.contains("sent you")
                             
            // Yeh words NAHI hone chahiye (Paise bhejne ya ad ke keywords)
            val isNotSent = !fullTextLower.contains("paid to") && !fullTextLower.contains("sent to") && 
                            !fullTextLower.contains("debited") && !fullTextLower.contains("cashback won")

            // Agar paisa aaya hai, tabhi aage badho
            if (hasMoneySymbol && isReceived && isNotSent) {
                // Bolne ke liye clean text banate hain
                val announcement = "$title. $text"
                speakOut(announcement)
            }
        }
    }

    private fun speakOut(text: String) {
        // 🚀 UPGRADE 3: QUEUE_ADD ka use kiya hai. 
        // Agar ek saath 3 log paise bhejenge, toh phone teeno ka naam line se bolega (pehle cut jata tha).
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN") // Hindi accent for better Indian names
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}