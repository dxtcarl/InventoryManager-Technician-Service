package com.Teltech.inventorymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.Teltech.inventorymanager.data.local.entity.AiMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiDao {
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiMessageEntity>>

    @Insert
    suspend fun insertMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages")
    suspend fun clearHistory()
}