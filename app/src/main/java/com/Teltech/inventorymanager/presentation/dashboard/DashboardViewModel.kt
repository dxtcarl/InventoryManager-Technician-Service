package com.Teltech.inventorymanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import com.Teltech.inventorymanager.domain.usecase.GetDashboardDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: InventoryRepository,
    getDashboardData: GetDashboardDataUseCase
) : ViewModel() {

    val state: StateFlow<GetDashboardDataUseCase.DashboardData> =
        getDashboardData()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GetDashboardDataUseCase.DashboardData(0.0, 0.0, 0, 0, 0, emptyList(), emptyList(), emptyList(), emptyList(), 0)
            )

    val userProfile = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
