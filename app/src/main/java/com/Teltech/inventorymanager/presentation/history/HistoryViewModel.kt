package com.Teltech.inventorymanager.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Teltech.inventorymanager.domain.model.Transaction
import com.Teltech.inventorymanager.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = combine(
        repository.getTransactionsWithProduct(),
        _searchQuery
    ) { transactions, query ->
        if (query.isBlank()) {
            transactions
        } else {
            transactions.filter {
                it.productName.contains(query, ignoreCase = true) ||
                it.note.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}