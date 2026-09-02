package com.Teltech.inventorymanager.domain.usecase

import com.Teltech.inventorymanager.domain.model.Product
import com.Teltech.inventorymanager.domain.model.Transaction
import com.Teltech.inventorymanager.domain.model.TransactionType
import com.Teltech.inventorymanager.domain.model.RepairJob
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

class GetDashboardDataUseCase(
    private val repository: InventoryRepository
) {
    data class DashboardData(
        val stockGrowthPercentage: Double,
        val totalStockValue: Double,
        val totalStockQuantity: Int,
        val outOfStockCount: Int,
        val lowStockCount: Int,
        val weeklyFlow: List<Double>,
        val flowLabels: List<String>,
        val topCustomers: List<Pair<String, Int>>,
        val topParts: List<Pair<String, Int>>,
        val customerCount: Int
    )

    operator fun invoke(): Flow<DashboardData> {
        return combine(
            repository.getAllProducts(),
            repository.getTransactionsWithProduct(), // Changed from getAllTransactions to get names
            repository.getAllRepairJobs(),
            repository.getAllCustomers()
        ) { products, transactions, repairs, customers ->
            val totalQty = products.sumOf { it.quantity }
            
            // --- CALCULATION LOGIC: STOCK GROWTH ---
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val recentTransactions = transactions.filter { it.timestamp >= sevenDaysAgo }
            val totalIn = recentTransactions.filter { it.type == TransactionType.IN }.sumOf { it.quantity }
            val totalOut = recentTransactions.filter { it.type == TransactionType.OUT }.sumOf { it.quantity }
            val netChange = totalIn - totalOut
            val previousStock = totalQty - netChange
            val growthPercentage = if (previousStock > 0) {
                (netChange.toDouble() / previousStock) * 100
            } else if (netChange > 0) {
                100.0
            } else {
                0.0
            }

            // --- CALCULATION LOGIC: TOP CUSTOMERS ---
            val topCustomers = repairs.groupBy { it.customerName }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(3)

            // --- CALCULATION LOGIC: TOP PARTS ---
            // We find parts used in repairs by looking at Transactions with "Job #" in notes
            val topParts = transactions.filter { it.type == TransactionType.OUT && it.note.contains("Job #") }
                .groupBy { it.productName }
                .mapValues { it.value.sumOf { t -> t.quantity } }
                .toList()
                .sortedByDescending { it.second }
                .take(3)

            // --- CALCULATION LOGIC: WEEKLY FLOW ---
            val calendar = Calendar.getInstance()
            val dailyValues = mutableListOf<Double>()
            val labels = mutableListOf<String>()
            
            for (i in 6 downTo 0) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val dayStart = calendar.clone() as Calendar
                dayStart.set(Calendar.HOUR_OF_DAY, 0)
                dayStart.set(Calendar.MINUTE, 0)
                dayStart.set(Calendar.SECOND, 0)
                
                val dayEnd = calendar.clone() as Calendar
                dayEnd.set(Calendar.HOUR_OF_DAY, 23)
                dayEnd.set(Calendar.MINUTE, 59)
                dayEnd.set(Calendar.SECOND, 59)

                val activity = transactions.filter { it.timestamp in dayStart.timeInMillis..dayEnd.timeInMillis }
                    .sumOf { it.quantity }.toDouble()
                
                dailyValues.add(activity)
                labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time))
            }

            DashboardData(
                stockGrowthPercentage = growthPercentage,
                totalStockValue = products.sumOf { it.totalValue },
                totalStockQuantity = totalQty,
                outOfStockCount = products.count { it.quantity == 0 },
                lowStockCount = products.count { it.isLowStock && it.quantity > 0 },
                weeklyFlow = dailyValues,
                flowLabels = labels,
                topCustomers = topCustomers,
                topParts = topParts,
                customerCount = customers.size
            )
        }
    }
}