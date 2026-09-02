package com.Teltech.inventorymanager.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Teltech.inventorymanager.domain.model.UserProfile
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveProfile(name: String, imageUri: String?) {
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(name, imageUri))
        }
    }
}