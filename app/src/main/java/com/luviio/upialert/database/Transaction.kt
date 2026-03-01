package com.luviio.upialert.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String,
    val sender: String,
    val upiApp: String,
    val transactionId: String,
    var timestamp: Long
) {
    fun getFormattedTime(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }
    
    fun getAmountDouble(): Double {
        return amount.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }
}