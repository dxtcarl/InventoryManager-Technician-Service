package com.Teltech.inventorymanager.domain.usecase

import com.Teltech.inventorymanager.domain.model.Product
import com.Teltech.inventorymanager.domain.repository.InventoryRepository

class AddProductUseCase(
    private val repository: InventoryRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        return when {
            product.name.isBlank() -> Result.failure(Error("Name cannot be empty"))
            product.sku.isBlank() -> Result.failure(Error("SKU cannot be empty"))
            product.price <= 0 -> Result.failure(Error("Price must be greater than 0"))
            product.quantity < 0 -> Result.failure(Error("Quantity cannot be negative"))
            else -> {
                repository.addProduct(product.copy(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
                Result.success(Unit)
            }
        }
    }
}