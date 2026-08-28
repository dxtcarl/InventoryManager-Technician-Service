package com.Teltech.inventorymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Product::class,
        parentColumns = ["id"],
        childColumns = ["productId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val type: String, // "IN" or "OUT"
    val quantity: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)