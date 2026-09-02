package com.Teltech.inventorymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.Teltech.inventorymanager.data.local.entity.TransactionEntity
import com.Teltech.inventorymanager.data.local.entity.TransactionWithProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun addTransaction(transaction: TransactionEntity)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 50")
    fun getAllTransactionsWithProduct(): Flow<List<TransactionWithProduct>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 50")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<TransactionEntity>>
}