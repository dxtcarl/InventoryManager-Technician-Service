package com.Teltech.inventorymanager.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RepairJobWithParts(
    @Embedded val job: RepairJobEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "repairJobId"
    )
    val parts: List<RepairPartEntity>,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity
)