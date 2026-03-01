package com.luviio.upialert.utils

import android.util.Log
import com.luviio.upialert.database.Transaction
import java.util.*

class NotificationParser {
    
    private val TAG = "LU VIIO Parser"
    
    fun parseUPINotification(
        text: String, 
        title: String, 
        packageName: String
    ): Transaction? {
        
        val amount = extractAmount(text)
        if (amount.isEmpty()) {
            return null
        }
        
        val sender = extractSender(text, title)
        val transactionId = extractTransactionId(text)
        
        return Transaction(
            amount = amount,
            sender = sender,
            upiApp = getAppName(packageName),
            transactionId = transactionId,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun extractAmount(text: String): String {
        val patterns = listOf(
            "₹\\s?([0-9,]+(?:\\.[0-9]{2})?)".toRegex(),
            "Rs\\.?\\s?([0-9,]+(?:\\.[0-9]{2})?)".toRegex(RegexOption.IGNORE_CASE),
            "([0-9,]+(?:\\.[0-9]{2})?)\\s?(rupees|rs)".toRegex(RegexOption.IGNORE_CASE),
            "paid\\s?₹?\\s?([0-9,]+)".toRegex(RegexOption.IGNORE_CASE),
            "received\\s?₹?\\s?([0-9,]+)".toRegex(RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amount = if (match.groupValues.size > 1) 
                    match.groupValues[1] 
                else 
                    match.value
                
                return amount.replace(",", "").trim()
            }
        }
        return ""
    }
    
    private fun extractSender(text: String, title: String): String {
        val senderPatterns = listOf(
            "from\\s+([A-Za-z\\s]+?)(?=\\s*(?:[₹]|$))".toRegex(RegexOption.IGNORE_CASE),
            "to\\s+([A-Za-z\\s]+?)(?=\\s*(?:[₹]|$))".toRegex(RegexOption.IGNORE_CASE),
            "by\\s+([A-Za-z\\s]+?)(?=\\s*(?:[₹]|$))".toRegex(RegexOption.IGNORE_CASE),
            "received\\s+from\\s+([A-Za-z\\s]+)".toRegex(RegexOption.IGNORE_CASE)
        )
        
        for (pattern in senderPatterns) {
            val match = pattern.find(text)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }
        
        return "Customer"
    }
    
    private fun extractTransactionId(text: String): String {
        val patterns = listOf(
            "Ref[^0-9]*([A-Z0-9]{6,})".toRegex(RegexOption.IGNORE_CASE),
            "ID[^0-9]*([A-Z0-9]{6,})".toRegex(RegexOption.IGNORE_CASE),
            "([A-Z0-9]{10,})".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return if (match.groupValues.size > 1) 
                    match.groupValues[1] 
                else 
                    match.value
            }
        }
        
        return "LUVIIO${System.currentTimeMillis()}"
    }
    
    private fun getAppName(packageName: String): String {
        return when (packageName) {
            "net.one97.paytm" -> "Paytm"
            "com.phonepe.app" -> "PhonePe"
            "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
            "com.bhim" -> "BHIM"
            "in.amazon.mShop.android.shopping" -> "Amazon Pay"
            "com.whatsapp" -> "WhatsApp"
            else -> "UPI App"
        }
    }
}