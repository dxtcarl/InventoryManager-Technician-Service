package com.Teltech.inventorymanager.presentation.products

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.Teltech.inventorymanager.domain.model.Product
import com.Teltech.inventorymanager.util.ImageUtils
import com.google.mlkit.vision.barcode.BarcodeScanning
import java.util.concurrent.Executors

@Composable
fun ProductsScreen(viewModel: ProductsViewModel = hiltViewModel()) {
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(allProducts, filter, searchQuery) {
        allProducts.filter { product ->
            val matchesFilter = when (filter) {
                "Out of Stock" -> product.quantity == 0
                "Low Stock" -> product.isLowStock && product.quantity > 0
                "Total Stock" -> true
                else -> true
            }
            val matchesSearch = product.name.contains(searchQuery, ignoreCase = true) || 
                               product.sku.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFA594F9),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Inventory", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8E2E4),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Product", fontSize = 12.sp)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Search Inventory", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true
            )

            // Filter Bar
            FilterBar(
                selectedFilter = filter,
                onFilterSelected = viewModel::setFilter,
                counts = mapOf(
                    "Total Stock" to allProducts.size,
                    "Out of Stock" to allProducts.count { it.quantity == 0 },
                    "Low Stock" to allProducts.count { it.isLowStock && it.quantity > 0 }
                )
            )

            message?.let {
                Text(it, color = if (it.startsWith("Error") || it.contains("Insufficient")) Color.Red else Color(0xFF4CAF50))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onEdit = { editingProduct = it }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProductDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { product ->
                viewModel.addProduct(product)
                showAddDialog = false
            }
        )
    }

    editingProduct?.let { product ->
        ProductDialog(
            productToEdit = product,
            categories = categories,
            onDismiss = { editingProduct = null },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                editingProduct = null
            },
            onDelete = {
                viewModel.deleteProduct(product.id)
                editingProduct = null
            }
        )
    }
}

@Composable
fun FilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int>
) {
    val filters = listOf("Total Stock", "Out of Stock", "Low Stock")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFFA594F9) else Color.White,
                contentColor = if (isSelected) Color.White else Color.Black,
                tonalElevation = 2.dp,
                shadowElevation = if (isSelected) 4.dp else 1.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFFF3F0FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            counts[filter]?.toString() ?: "0",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFFA594F9)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onEdit: (Product) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(product) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUri != null) {
                    AsyncImage(
                        model = product.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("📦", fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    val (statusText, statusColor, bgColor) = when {
                        product.quantity == 0 -> Triple("Out of Stock", Color(0xFFFF4D4D), Color(0xFFFFEBEB))
                        product.isLowStock -> Triple("Low Stock", Color(0xFFFF9800), Color(0xFFFFF3E0))
                        else -> Triple("In Stock", Color(0xFF4CAF50), Color(0xFFE8F5E9))
                    }
                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₱ %.2f".format(product.price), fontWeight = FontWeight.Bold, color = Color(0xFFA594F9))
                    Spacer(Modifier.width(8.dp))
                    Text("•", color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("${product.quantity} Units", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("+2.5% this month", color = Color(0xFF4CAF50), fontSize = 10.sp)
                    IconButton(onClick = { onEdit(product) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDialog(
    productToEdit: Product? = null,
    categories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var sku by remember { mutableStateOf(productToEdit?.sku ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "") }
    var price by remember { mutableStateOf(productToEdit?.price?.toString() ?: "") }
    var quantity by remember { mutableIntStateOf(productToEdit?.quantity ?: 0) }
    var quantityText by remember { mutableStateOf(productToEdit?.quantity?.toString() ?: "0") }
    var supplier by remember { mutableStateOf(productToEdit?.supplier ?: "") }
    var imageUri by remember { mutableStateOf<String?>(productToEdit?.imageUri) }
    var showScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = ImageUtils.saveImageToInternalStorage(context, it)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) showScanner = true }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            BarcodeScannerView(onBarcodeDetected = { barcode -> sku = barcode; showScanner = false }, onClose = { showScanner = false })
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (productToEdit == null) "New Product" else "Edit Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFA594F9)
                    )
                    Row {
                        if (productToEdit != null && onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFA594F9).copy(alpha = 0.1f))

                // Image Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Click to upload image", color = Color.Gray, fontSize = 12.sp)
                            Button(
                                onClick = { launcher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F0FF), contentColor = Color(0xFFA594F9)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Choose File", fontSize = 10.sp)
                            }
                        }
                    }
                }

                StyledTextField(value = name, onValueChange = { name = it }, label = "Product Name")
                
                Column {
                    StyledTextField(value = category, onValueChange = { category = it }, label = "Category")
                    if (categories.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories.filter { it.isNotBlank() }) { existingCat ->
                                AssistChip(
                                    onClick = { category = existingCat },
                                    label = { Text(existingCat, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.White,
                                        labelColor = Color(0xFFA594F9)
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StyledTextField(value = sku, onValueChange = { sku = it }, label = "SKU", modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) showScanner = true
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.height(56.dp).padding(top = 28.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Scan")
                    }
                }

                Column {
                    Text("Quantity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            if (quantity > 0) quantity--
                            quantityText = quantity.toString()
                        }) { Text("-", fontSize = 24.sp) }

                        BasicTextField(
                            value = quantityText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    quantityText = input
                                    quantity = input.toIntOrNull() ?: 0
                                }
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(60.dp)
                        )

                        IconButton(onClick = {
                            quantity++
                            quantityText = quantity.toString()
                        }) { Text("+", fontSize = 24.sp) }
                    }
                }

                StyledTextField(value = supplier, onValueChange = { supplier = it }, label = "Supplier Name")
                StyledTextField(value = price, onValueChange = { price = it }, label = "Selling Price")

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val product = if (productToEdit != null) {
                            productToEdit.copy(name = name, sku = sku, category = category, price = price.toDoubleOrNull() ?: 0.0, quantity = quantity, supplier = supplier, imageUri = imageUri, updatedAt = System.currentTimeMillis())
                        } else {
                            Product(name = name, sku = sku, category = category, price = price.toDoubleOrNull() ?: 0.0, quantity = quantity, supplier = supplier, imageUri = imageUri)
                        }
                        onConfirm(product)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA594F9))
                ) {
                    Text(if (productToEdit == null) "Save Product" else "Update Product", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StyledTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor, MlKitAnalyzer(
                        listOf(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                        cameraExecutor
                    ) { result ->
                        val barcode = result.getValue(barcodeScanner)?.firstOrNull()
                        barcode?.rawValue?.let {
                            onBarcodeDetected(it)
                        }
                    })

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Close")
        }
    }
}
