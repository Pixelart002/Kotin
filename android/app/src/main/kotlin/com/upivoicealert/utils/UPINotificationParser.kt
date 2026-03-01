package com.upivoicealert.utils

import android.util.Log
import java.util.regex.Pattern

class UPINotificationParser {
    private val TAG = "UPIParser"

    fun parseNotification(title: String, text: String, subText: String): Map<String, String>? {
        try {
            val fullMessage = "$title $text $subText".trim()
            
            // Extract sender/receiver name
            val senderName = extractName(fullMessage)
            
            // Extract amount
            val amount = extractAmount(fullMessage)
            
            // Extract transaction type
            val transactionType = extractTransactionType(fullMessage)
            
            if (senderName.isNotEmpty() && amount.isNotEmpty()) {
                return mapOf(
                    "senderName" to senderName,
                    "amount" to amount,
                    "type" to transactionType
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing notification", e)
        }
        
        return null
    }

    private fun extractName(message: String): String {
        // Pattern to extract names - handles various formats
        val patterns = listOf(
            "(?:from|to|by)\\s+([A-Za-z\\s]+)(?:\\s*(?:sent|received|paid|transferred))?",
            "([A-Za-z\\s]+)\\s+(?:sent|received|paid|transferred)",
            "^([A-Za-z\\s]+)\\s+(?:has|have)"
        )
        
        for (patternStr in patterns) {
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group(1).trim()
            }
        }
        
        return "User"
    }

    private fun extractAmount(message: String): String {
        // Pattern to extract currency amounts (₹, Rs, Rs., etc.)
        val patterns = listOf(
            "[₹Rr][sS]?\\.?\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            "([0-9,]+(?:\\.[0-9]{2})?)\\s*(?:[₹Rr][sS]?)",
            "(?:amount|rupees|₹)\\s*[:\\s]*([0-9,]+(?:\\.[0-9]{2})?)"
        )
        
        for (patternStr in patterns) {
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val amount = matcher.group(1).replace(",", "")
                return "rupees $amount"
            }
        }
        
        return ""
    }

    private fun extractTransactionType(message: String): String {
        return when {
            message.lowercase().contains("received") -> "received"
            message.lowercase().contains("sent") -> "sent"
            message.lowercase().contains("paid") -> "paid"
            message.lowercase().contains("transferred") -> "transferred"
            message.lowercase().contains("refund") -> "refund"
            else -> "transaction"
        }
    }
}
