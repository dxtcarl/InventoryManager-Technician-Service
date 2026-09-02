package com.Teltech.inventorymanager.domain.repository

import com.Teltech.inventorymanager.data.local.dao.ProductDao
import com.Teltech.inventorymanager.data.local.dao.RepairDao
import com.Teltech.inventorymanager.data.local.dao.TransactionDao
import com.Teltech.inventorymanager.data.local.dao.UserDao
import com.Teltech.inventorymanager.data.local.entity.*
import com.Teltech.inventorymanager.domain.model.*
import com.Teltech.inventorymanager.domain.model.Product as DomainProduct
import com.Teltech.inventorymanager.domain.model.Transaction as DomainTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val userDao: UserDao,
    private val repairDao: RepairDao
) : InventoryRepository {

    override fun getUserProfile(): Flow<UserProfile?> =
        userDao.getUserProfile().map { entity ->
            entity?.let { UserProfile(name = it.name, profilePictureUri = it.profilePictureUri) }
        }

    override suspend fun saveUserProfile(userProfile: UserProfile) {
        userDao.saveUserProfile(UserProfileEntity(name = userProfile.name, profilePictureUri = userProfile.profilePictureUri))
    }

    private fun ProductEntity.toDomain() = DomainProduct(
        id = id, name = name, sku = sku, category = category,
        price = price, quantity = quantity, minStockThreshold = minStockThreshold,
        supplier = supplier, imageUri = imageUri, createdAt = createdAt, updatedAt = updatedAt
    )
    private fun DomainProduct.toEntity() = ProductEntity(
        id = id, name = name, sku = sku, category = category,
        price = price, quantity = quantity, minStockThreshold = minStockThreshold,
        supplier = supplier, imageUri = imageUri, createdAt = createdAt, updatedAt = updatedAt
    )
    private fun TransactionEntity.toDomain() = DomainTransaction(
        id = id, productId = productId,
        type = if (type == "IN") TransactionType.IN else TransactionType.OUT,
        quantity = quantity, note = note, timestamp = timestamp
    )

    override fun getAllProducts(): Flow<List<DomainProduct>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    override fun getLowStockProducts(): Flow<List<DomainProduct>> =
        productDao.getLowStockProducts().map { list -> list.map { it.toDomain() } }

    override suspend fun addProduct(product: DomainProduct) {
        val id = productDao.addProduct(product.toEntity())
        if (product.quantity > 0) {
            transactionDao.addTransaction(
                TransactionEntity(
                    productId = id,
                    type = "IN",
                    quantity = product.quantity,
                    note = "Initial Stock"
                )
            )
        }
    }

    override suspend fun updateProduct(product: DomainProduct) {
        val oldProduct = productDao.getProductById(product.id)
        productDao.updateProduct(product.toEntity().copy(updatedAt = System.currentTimeMillis()))
        
        if (oldProduct != null && oldProduct.quantity != product.quantity) {
            val diff = product.quantity - oldProduct.quantity
            transactionDao.addTransaction(
                TransactionEntity(
                    productId = product.id,
                    type = if (diff > 0) "IN" else "OUT",
                    quantity = kotlin.math.abs(diff),
                    note = "Manual Edit"
                )
            )
        }
    }

    override suspend fun deleteProduct(productId: Long) =
        productDao.deleteProduct(productId)

    override suspend fun getProductById(productId: Long): DomainProduct? =
        productDao.getProductById(productId)?.toDomain()

    override fun searchProducts(query: String): Flow<List<DomainProduct>> =
        productDao.searchProducts(query).map { list -> list.map { it.toDomain() } }

    override suspend fun stockIn(productId: Long, quantity: Int, note: String) {
        val product = getProductById(productId) ?: return
        productDao.updateProduct(product.toEntity().copy(
            quantity = product.quantity + quantity,
            updatedAt = System.currentTimeMillis()
        ))
        transactionDao.addTransaction(
            TransactionEntity(
                productId = productId, type = "IN", quantity = quantity, note = note
            )
        )
    }

    override suspend fun stockOut(productId: Long, quantity: Int, note: String) {
        val product = getProductById(productId) ?: return
        productDao.updateProduct(product.toEntity().copy(
            quantity = product.quantity - quantity,
            updatedAt = System.currentTimeMillis()
        ))
        transactionDao.addTransaction(
            TransactionEntity(
                productId = productId, type = "OUT", quantity = quantity, note = note
            )
        )
    }

    override fun getAllTransactions(): Flow<List<DomainTransaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsWithProduct(): Flow<List<DomainTransaction>> =
        transactionDao.getAllTransactionsWithProduct().map { list ->
            list.map { item ->
                item.transaction.toDomain().copy(productName = item.product.name)
            }
        }

    override fun getTransactionsForProduct(productId: Long): Flow<List<DomainTransaction>> =
        transactionDao.getTransactionsForProduct(productId).map { list -> list.map { it.toDomain() } }

    // Repair Implementation
    override fun getAllCustomers(): Flow<List<Customer>> =
        repairDao.getAllCustomers().map { list ->
            list.map { Customer(it.id, it.name, it.phoneNumber, it.email) }
        }

    override suspend fun addCustomer(customer: Customer): Long =
        repairDao.insertCustomer(CustomerEntity(name = customer.name, phoneNumber = customer.phoneNumber, email = customer.email))

    override fun getAllRepairJobs(): Flow<List<RepairJob>> =
        repairDao.getAllRepairJobsWithParts().map { list ->
            list.map { item ->
                val partsTotal = item.parts.sumOf { it.priceAtTime * it.quantity }
                RepairJob(
                    id = item.job.id,
                    customerId = item.job.customerId,
                    customerName = item.customer.name,
                    deviceModel = item.job.deviceModel,
                    issueDescription = item.job.issueDescription,
                    status = RepairStatus.valueOf(item.job.status),
                    laborCost = item.job.laborCost,
                    totalCost = item.job.laborCost + partsTotal,
                    createdAt = item.job.createdAt,
                    completedAt = item.job.completedAt
                )
            }
        }

    override suspend fun createRepairJob(job: RepairJob, usedParts: List<DomainProduct>): Long {
        val jobId = repairDao.insertRepairJob(RepairJobEntity(
            customerId = job.customerId,
            deviceModel = job.deviceModel,
            issueDescription = job.issueDescription,
            status = job.status.name,
            laborCost = job.laborCost
        ))

        usedParts.forEach { part ->
            repairDao.insertRepairPart(RepairPartEntity(
                repairJobId = jobId,
                productId = part.id,
                quantity = 1,
                priceAtTime = part.price
            ))
            stockOut(part.id, 1, "Job #$jobId: ${job.deviceModel}")
        }
        return jobId
    }

    override suspend fun updateRepairStatus(jobId: Long, status: RepairStatus) {
        repairDao.updateRepairStatus(jobId, status.name)
    }

    override fun getAllCategories(): Flow<List<String>> =
        productDao.getAllCategories()

    override suspend fun getBusinessContext(): String {
        val products = productDao.getAllProducts().first()
        val transactions = transactionDao.getAllTransactionsWithProduct().first()
        val repairs = repairDao.getAllRepairJobsWithParts().first()
        
        val productInfo = products.joinToString("\n") { 
            "- ${it.name} (SKU: ${it.sku}): Qty ${it.quantity}, Price P${it.price}, Category: ${it.category}" 
        }
        
        val repairInfo = repairs.joinToString("\n") { 
            "- Job #${it.job.id}: ${it.job.deviceModel} for ${it.customer.name}. Status: ${it.job.status}. Labor: P${it.job.laborCost}" 
        }

        return """
            Current Business Inventory and Repairs Data:
            
            PRODUCTS:
            $productInfo
            
            RECENT REPAIRS:
            $repairInfo
            
            TRANSACTION LOGS:
            ${transactions.take(10).joinToString("\n") { "- ${it.product.name}: ${it.transaction.type} ${it.transaction.quantity} on ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.transaction.timestamp))}" }}
        """.trimIndent()
    }
}