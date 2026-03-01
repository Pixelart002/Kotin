package com.upivoicealert.services

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.upivoicealert.utils.TTSEngine
import com.upivoicealert.utils.UPINotificationParser

class UPINotificationListenerService : NotificationListenerService() {
    private val TAG = "UPINotificationListener"
    private val upiParser = UPINotificationParser()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        try {
            val packageName = sbn.packageName
            Log.d(TAG, "Notification from: $packageName")
            
            // Check if notification is from UPI apps (Google Pay, PhonePe, Paytm, BHIM)
            if (isUPIApp(packageName)) {
                val notification = sbn.notification
                val extras = notification.extras
                
                val title = extras.getString("android.title") ?: ""
                val text = extras.getString("android.text") ?: ""
                val subText = extras.getString("android.subText") ?: ""
                
                Log.d(TAG, "Title: $title, Text: $text, SubText: $subText")
                
                // Parse UPI transaction details
                val upiData = upiParser.parseNotification(title, text, subText)
                
                if (upiData != null) {
                    // Generate announcement
                    val announcement = generateAnnouncement(upiData)
                    Log.d(TAG, "Announcement: $announcement")
                    
                    // Announce via TTS
                    TTSEngine.speak(announcement)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    private fun isUPIApp(packageName: String): Boolean {
        val upiApps = listOf(
            "com.google.android.apps.nbu.paisa.user",  // Google Pay
            "com.phonepe.app",                          // PhonePe
            "com.paytm.android",                        // Paytm
            "in.org.npci.upiapp",                       // BHIM
            "com.amazon.mShop.android.shopping",        // Amazon Pay
            "com.whatsapp",                             // WhatsApp Payments
            "com.mobikwik_new"                          // MobiKwik
        )
        return upiApps.contains(packageName)
    }

    private fun generateAnnouncement(upiData: Map<String, String>): String {
        val senderName = upiData["senderName"] ?: "Unknown"
        val amount = upiData["amount"] ?: "Unknown amount"
        val transactionType = upiData["type"] ?: "Payment"
        
        return when (transactionType.lowercase()) {
            "received", "received money" -> "Received $amount from $senderName"
            "sent", "sent money" -> "Sent $amount to $senderName"
            else -> "$senderName, transaction of $amount, $transactionType"
        }
    }
}
