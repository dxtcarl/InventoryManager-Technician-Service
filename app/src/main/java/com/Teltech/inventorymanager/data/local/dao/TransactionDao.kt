package com.Teltech.inventorymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yourname.inventorymanager.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun addTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 50")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<Transaction>>
}