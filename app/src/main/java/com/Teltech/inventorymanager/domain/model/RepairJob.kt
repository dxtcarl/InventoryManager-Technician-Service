package com.Teltech.inventorymanager.domain.model

data class RepairJob(
    val id: Long = 0,
    val customerId: Long,
    val customerName: String = "",
    val deviceModel: String,
    val issueDescription: String,
    val status: RepairStatus = RepairStatus.PENDING,
    val laborCost: Double = 0.0,
    val totalCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class RepairStatus {
    PENDING, REPAIRING, REPAIRED, RELEASED
}