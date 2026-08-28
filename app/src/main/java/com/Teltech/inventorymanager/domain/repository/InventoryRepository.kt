package com.Teltech.inventorymanager.domain.repository

import com.yourname.inventorymanager.domain.model.Product
import com.yourname.inventorymanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getLowStockProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: Long)
    suspend fun getProductById(productId: Long): Product?
    fun searchProducts(query: String): Flow<List<Product>>

    suspend fun stockIn(productId: Long, quantity: Int, note: String = "")
    suspend fun stockOut(productId: Long, quantity: Int, note: String = "")
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsForProduct(productId: Long): Flow<List<Transaction>>
}