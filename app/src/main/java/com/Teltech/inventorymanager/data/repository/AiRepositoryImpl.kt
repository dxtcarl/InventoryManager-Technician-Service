package com.Teltech.inventorymanager.data.repository

import com.Teltech.inventorymanager.data.local.dao.AiDao
import com.Teltech.inventorymanager.data.local.entity.AiMessageEntity
import com.Teltech.inventorymanager.domain.model.AiMessage
import com.Teltech.inventorymanager.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AiRepositoryImpl(
    private val apiKey: String,
    private val aiDao: AiDao
) : AiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    override fun getChatHistory(): Flow<List<AiMessage>> =
        aiDao.getAllMessages().map { list ->
            list.map { AiMessage(it.text, it.isUser, it.timestamp) }
        }

    override suspend fun saveMessage(message: AiMessage) {
        aiDao.insertMessage(AiMessageEntity(text = message.text, isUser = message.isUser, timestamp = message.timestamp))
    }

    override suspend fun clearHistory() {
        aiDao.clearHistory()
    }

    override suspend fun getChatResponse(prompt: String, context: String): String {
        return try {
            val fullPrompt = """
                You are a smart business assistant for "Teltech Inventory Manager", an app for a phone and laptop technician.
                Use the following business data to answer the user's question accurately.
                
                BUSINESS DATA:
                $context
                
                USER QUESTION:
                $prompt
            """.trimIndent()

            val response = model.generateContent(fullPrompt)
            response.text ?: "I'm sorry, I couldn't generate a response."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Unknown AI error"}"
        }
    }
}