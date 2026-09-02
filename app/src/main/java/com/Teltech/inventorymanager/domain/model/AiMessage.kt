package com.Teltech.inventorymanager.domain.model

data class AiMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)