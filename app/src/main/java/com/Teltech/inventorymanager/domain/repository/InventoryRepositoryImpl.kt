package com.Teltech.inventorymanager.data.repository

import com.yourname.inventorymanager.data.local.dao.ProductDao
import com.yourname.inventorymanager.data.local.dao.TransactionDao
import com.yourname.inventorymanager.data.local.entity.ProductEntity
import com.yourname.inventorymanager.data.local.entity.TransactionEntity
import com.yourname.inventorymanager.data.local.entity.TransactionTypeEntity
import com.yourname.inventorymanager.domain.model.Product
import com.yourname.inventorymanager.domain.model.Transaction
import com.yourname.inventorymanager.domain.model.TransactionType
import com.yourname.inventorymanager.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InventoryRepositoryImpl(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) : InventoryRepository {

    // Mapping: Entity → Domain
    private fun ProductEntity.toDomain() = Product(
        id = id, name = name, sku = sku, category = category,
        price = price, quantity = quantity, minStockThreshold = minStockThreshold,
        supplier = supplier, createdAt = createdAt, updatedAt = updatedAt
    )
    private fun Product.toEntity() = ProductEntity(
        id = id, name = name, sku = sku, category = category,
        price = price, quantity = quantity, minStockThreshold = minStockThreshold,
        supplier = supplier, createdAt = createdAt, updatedAt = updatedAt
    )
    private fun TransactionEntity.toDomain() = Transaction(
        id = id, productId = productId,
        type = if (type == "IN") TransactionType.IN else TransactionType.OUT,
        quantity = quantity, note = note, timestamp = timestamp
    )

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    override fun getLowStockProducts(): Flow<List<Product>> =
        productDao.getLowStockProducts().map { list -> list.map { it.toDomain() } }

    override suspend fun addProduct(product: Product) =
        productDao.addProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deleteProduct(productId: Long) =
        productDao.deleteProduct(productId)

    override suspend fun getProductById(productId: Long): Product? =
        productDao.getProductById(productId)?.toDomain()

    override fun searchProducts(query: String): Flow<List<Product>> =
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

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsForProduct(productId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForProduct(productId).map { list -> list.map { it.toDomain() } }
}