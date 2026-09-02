package com.Teltech.inventorymanager.data.local.dao

import androidx.room.*
import com.Teltech.inventorymanager.data.local.entity.CustomerEntity
import com.Teltech.inventorymanager.data.local.entity.RepairJobEntity
import com.Teltech.inventorymanager.data.local.entity.RepairJobWithParts
import com.Teltech.inventorymanager.data.local.entity.RepairPartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {
    @Insert
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Insert
    suspend fun insertRepairJob(job: RepairJobEntity): Long

    @Update
    suspend fun updateRepairJob(job: RepairJobEntity)

    @Query("SELECT * FROM repair_jobs ORDER BY createdAt DESC")
    fun getAllRepairJobs(): Flow<List<RepairJobEntity>>

    @Transaction
    @Query("SELECT * FROM repair_jobs ORDER BY createdAt DESC")
    fun getAllRepairJobsWithParts(): Flow<List<RepairJobWithParts>>

    @Insert
    suspend fun insertRepairPart(part: RepairPartEntity)

    @Query("SELECT * FROM repair_parts WHERE repairJobId = :jobId")
    suspend fun getPartsForJob(jobId: Long): List<RepairPartEntity>

    @Query("UPDATE repair_jobs SET status = :status WHERE id = :jobId")
    suspend fun updateRepairStatus(jobId: Long, status: String)
}