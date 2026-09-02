package com.Teltech.inventorymanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repair_jobs",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class RepairJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val deviceModel: String,
    val issueDescription: String,
    val status: String, // Maps to RepairStatus enum
    val laborCost: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)