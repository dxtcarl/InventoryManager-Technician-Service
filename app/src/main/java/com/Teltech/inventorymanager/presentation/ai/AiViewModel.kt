package com.Teltech.inventorymanager.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Teltech.inventorymanager.domain.model.AiMessage
import com.Teltech.inventorymanager.domain.repository.AiRepository
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    val messages: StateFlow<List<AiMessage>> = aiRepository.getChatHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMessage = AiMessage(text, true)
        
        viewModelScope.launch {
            aiRepository.saveMessage(userMessage)
            _isLoading.value = true
            
            val context = inventoryRepository.getBusinessContext()
            val responseText = aiRepository.getChatResponse(text, context)
            
            aiRepository.saveMessage(AiMessage(responseText, false))
            _isLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            aiRepository.clearHistory()
        }
    }
}