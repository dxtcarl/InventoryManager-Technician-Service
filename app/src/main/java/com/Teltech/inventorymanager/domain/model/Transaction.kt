package com.Teltech.inventorymanager.domain.model

data class Transaction(
    val id: Long = 0,
    val productId: Long,
    val productName: String = "",
    val type: TransactionType,
    val quantity: Int,
    val note: String = "",
    val timestamp: Long = 0
)

enum class TransactionType {
    IN, OUT
}