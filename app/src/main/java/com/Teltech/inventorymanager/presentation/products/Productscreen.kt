package com.Teltech.inventorymanager.presentation.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourname.inventorymanager.domain.model.Product

@Composable
fun ProductsScreen(viewModel: ProductsViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📦 Inventory", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchChange,
            label = { Text("Search products...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        message?.let {
            Text(it, color = if (it.startsWith("Error") || it.contains("Insufficient")) Color.Red else Color.Green)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onStockIn = { qty -> viewModel.stockIn(product.id, qty) },
                    onStockOut = { qty -> viewModel.stockOut(product.id, qty) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onStockIn: (Int) -> Unit, onStockOut: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isLowStock) Color(0xFFFFE5E5) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("₱ %.2f".format(product.price))
            }
            Text("SKU: ${product.sku}  •  Qty: ${product.quantity}")
            Text("Value: ₱ %.2f".format(product.totalValue))
            if (product.isLowStock) Text("⚠️ LOW STOCK", color = Color.Red, fontWeight = FontWeight.Bold)

            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStockIn(5) }) { Text("+5 IN") }
                Button(onClick = { onStockOut(5) }) { Text("-5 OUT") }
            }
        }
    }
}