package com.Teltech.inventorymanager.domain.repository

import com.Teltech.inventorymanager.domain.model.AiMessage
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun getChatHistory(): Flow<List<AiMessage>>
    suspend fun saveMessage(message: AiMessage)
    suspend fun clearHistory()
    suspend fun getChatResponse(prompt: String, context: String): String
}