package com.Teltech.inventorymanager.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val sku: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val minStockThreshold: Int = 5,
    val supplier: String = "",
    val imageUri: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    // Business logic right here in domain
    val totalValue: Double get() = price * quantity
    val isLowStock: Boolean get() = quantity <= minStockThreshold
}