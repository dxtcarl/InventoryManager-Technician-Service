package com.Teltech.inventorymanager.domain.usecase

import com.yourname.inventorymanager.domain.repository.InventoryRepository

class UpdateStockUseCase(
    private val repository: InventoryRepository
) {
    suspend fun stockIn(productId: Long, quantity: Int, note: String = ""): Result<Unit> {
        return when {
            quantity <= 0 -> Result.failure(Error("Quantity must be positive"))
            else -> {
                repository.stockIn(productId, quantity, note)
                Result.success(Unit)
            }
        }
    }

    suspend fun stockOut(productId: Long, quantity: Int, note: String = ""): Result<Unit> {
        val product = repository.getProductById(productId)
        return when {
            product == null -> Result.failure(Error("Product not found"))
            quantity <= 0 -> Result.failure(Error("Quantity must be positive"))
            quantity > product.quantity -> Result.failure(Error("Insufficient stock! Available: ${product.quantity}"))
            else -> {
                repository.stockOut(productId, quantity, note)
                Result.success(Unit)
            }
        }
    }
}