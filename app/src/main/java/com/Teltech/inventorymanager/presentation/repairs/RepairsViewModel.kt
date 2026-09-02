package com.Teltech.inventorymanager.presentation.repairs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Teltech.inventorymanager.domain.model.Customer
import com.Teltech.inventorymanager.domain.model.Product
import com.Teltech.inventorymanager.domain.model.RepairJob
import com.Teltech.inventorymanager.domain.model.RepairStatus
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepairsViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val repairJobs: StateFlow<List<RepairJob>> = combine(
        repository.getAllRepairJobs(),
        _searchQuery
    ) { jobs, query ->
        if (query.isBlank()) {
            jobs
        } else {
            jobs.filter {
                it.deviceModel.contains(query, ignoreCase = true) ||
                it.customerName.contains(query, ignoreCase = true) ||
                it.issueDescription.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLaborCost: StateFlow<Double> = repairJobs.map { jobs ->
        jobs.filter { it.status == RepairStatus.REPAIRED || it.status == RepairStatus.RELEASED }
            .sumOf { it.laborCost }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val customers: StateFlow<List<Customer>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableProducts: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun createCustomer(name: String, phone: String, email: String) {
        viewModelScope.launch {
            repository.addCustomer(Customer(name = name, phoneNumber = phone, email = email))
            _message.value = "Customer added!"
        }
    }

    fun createRepairJob(job: RepairJob, usedParts: List<Product>) {
        viewModelScope.launch {
            repository.createRepairJob(job, usedParts)
            _message.value = "Repair job created!"
        }
    }

    fun updateStatus(jobId: Long, status: RepairStatus) {
        viewModelScope.launch {
            repository.updateRepairStatus(jobId, status)
            _message.value = "Status updated to ${status.name}"
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearMessage() {
        _message.value = null
    }
}