package com.Teltech.inventorymanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.inventorymanager.domain.usecase.GetDashboardDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardData: GetDashboardDataUseCase
) : ViewModel() {

    val state: StateFlow<GetDashboardDataUseCase.DashboardData> =
        getDashboardData()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GetDashboardDataUseCase.DashboardData(0, 0.0, 0)
            )
}