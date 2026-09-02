package com.Teltech.inventorymanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repair_parts",
    foreignKeys = [
        ForeignKey(
            entity = RepairJobEntity::class,
            parentColumns = ["id"],
            childColumns = ["repairJobId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("repairJobId"), Index("productId")]
)
data class RepairPartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val repairJobId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val priceAtTime: Double // Store price when used in case inventory price changes
)