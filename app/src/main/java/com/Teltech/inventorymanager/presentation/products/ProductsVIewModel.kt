package com.Teltech.inventorymanager.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.inventorymanager.domain.model.Product
import com.yourname.inventorymanager.domain.repository.InventoryRepository
import com.yourname.inventorymanager.domain.usecase.AddProductUseCase
import com.yourname.inventorymanager.domain.usecase.UpdateStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val addProductUseCase: AddProductUseCase,
    private val updateStockUseCase: UpdateStockUseCase
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults = searchQuery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            val result = addProductUseCase(product)
            result.exceptionOrNull()?.let {
                _message.value = it.message
            } ?: run {
                _message.value = "Product added!"
            }
        }
    }

    fun stockIn(productId: Long, qty: Int, note: String = "") {
        viewModelScope.launch {
            updateStockUseCase.stockIn(productId, qty, note)
        }
    }

    fun stockOut(productId: Long, qty: Int, note: String = "") {
        viewModelScope.launch {
            val result = updateStockUseCase.stockOut(productId, qty, note)
            result.exceptionOrNull()?.let { _message.value = it.message }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}