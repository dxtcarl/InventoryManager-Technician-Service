package com.Teltech.inventorymanager.domain.repository

import com.Teltech.inventorymanager.domain.model.Customer
import com.Teltech.inventorymanager.domain.model.RepairJob
import com.Teltech.inventorymanager.domain.model.RepairStatus
import com.Teltech.inventorymanager.domain.model.Product
import com.Teltech.inventorymanager.domain.model.Transaction
import com.Teltech.inventorymanager.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(userProfile: UserProfile)

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
    fun getTransactionsWithProduct(): Flow<List<Transaction>>
    fun getTransactionsForProduct(productId: Long): Flow<List<Transaction>>

    // Repair Logic
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun addCustomer(customer: Customer): Long
    fun getAllRepairJobs(): Flow<List<RepairJob>>
    suspend fun createRepairJob(job: RepairJob, usedParts: List<Product>): Long
    suspend fun updateRepairStatus(jobId: Long, status: RepairStatus)

    fun getAllCategories(): Flow<List<String>>

    suspend fun getBusinessContext(): String
}