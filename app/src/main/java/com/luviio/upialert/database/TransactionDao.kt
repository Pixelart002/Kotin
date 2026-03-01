package com.luviio.upialert.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Insert
    suspend fun insert(transaction: Transaction)
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 100")
    fun getRecentTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT SUM(CAST(REPLACE(amount, ',', '') AS REAL)) FROM transactions WHERE timestamp > :startOfDay")
    suspend fun getTodayTotal(startOfDay: Long): Double
    
    @Query("SELECT SUM(CAST(REPLACE(amount, ',', '') AS REAL)) FROM transactions WHERE timestamp > :startOfWeek")
    suspend fun getWeekTotal(startOfWeek: Long): Double
    
    @Query("SELECT SUM(CAST(REPLACE(amount, ',', '') AS REAL)) FROM transactions WHERE timestamp > :startOfMonth")
    suspend fun getMonthTotal(startOfMonth: Long): Double
}