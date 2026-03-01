package com.luviio.upialert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luviio.upialert.database.AppDatabase
import com.luviio.upialert.database.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(private val database: AppDatabase) : ViewModel() {
    
    val recentTransactions: Flow<List<Transaction>> = database.transactionDao().getRecentTransactions()
    
    fun deleteOldTransactions(cutoffTime: Long) {
        viewModelScope.launch {
            database.transactionDao().deleteOldTransactions(cutoffTime)
        }
    }
    
    fun getTodayTotal(startOfDay: Long): Flow<Double> {
        return database.transactionDao().getTodayTotalFlow(startOfDay)
    }
}