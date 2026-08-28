package com.Teltech.inventorymanager.domain.usecase

import com.yourname.inventorymanager.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDashboardDataUseCase(
    private val repository: InventoryRepository
) {
    data class DashboardData(
        val totalProducts: Int,
        val totalStockValue: Double,
        val lowStockCount: Int
    )

    operator fun invoke(): Flow<DashboardData> {
        return repository.getAllProducts().map { products ->
            DashboardData(
                totalProducts = products.size,
                totalStockValue = products.sumOf { it.totalValue },
                lowStockCount = products.count { it.isLowStock }
            )
        }
    }
}