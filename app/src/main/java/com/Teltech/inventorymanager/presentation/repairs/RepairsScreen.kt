package com.Teltech.inventorymanager.presentation.repairs

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.Teltech.inventorymanager.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RepairsScreen(viewModel: RepairsViewModel = hiltViewModel()) {
    val repairJobs by viewModel.repairJobs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val totalLabor by viewModel.totalLaborCost.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showAddJobDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { showAddCustomerDialog = true },
                    containerColor = Color.White,
                    contentColor = Color(0xFFA594F9),
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = "Add Customer")
                }
                FloatingActionButton(
                    onClick = { showAddJobDialog = true },
                    containerColor = Color(0xFFA594F9),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Handyman, contentDescription = "New Repair")
                }
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
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Repairs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color(0xFFF3F0FF),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Labor: ₱ %.0f".format(totalLabor),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFFA594F9),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search Repairs", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(repairJobs) { job ->
                    RepairJobCard(
                        job = job,
                        onStatusChange = { newStatus -> viewModel.updateStatus(job.id, newStatus) }
                    )
                }
            }
        }
    }

    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, email ->
                viewModel.createCustomer(name, phone, email)
                showAddCustomerDialog = false
            }
        )
    }

    if (showAddJobDialog) {
        AddRepairJobDialog(
            customers = customers,
            availableProducts = viewModel.availableProducts.collectAsStateWithLifecycle().value,
            onDismiss = { showAddJobDialog = false },
            onConfirm = { job, parts ->
                viewModel.createRepairJob(job, parts)
                showAddJobDialog = false
            }
        )
    }
}

@Composable
fun RepairJobCard(job: RepairJob, onStatusChange: (RepairStatus) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val date = remember(job.createdAt) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(job.createdAt))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(job.deviceModel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Customer: ${job.customerName}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA594F9))
                }
                StatusBadge(job.status)
            }
            Text("Issue: ${job.issueDescription}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Labor: ₱ %.2f".format(job.laborCost), fontWeight = FontWeight.Bold, color = Color(0xFFA594F9))
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                Text("Update Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusButton("Pending", job.status == RepairStatus.PENDING) { onStatusChange(RepairStatus.PENDING) }
                    StatusButton("Repairing", job.status == RepairStatus.REPAIRING) { onStatusChange(RepairStatus.REPAIRING) }
                    StatusButton("Done", job.status == RepairStatus.REPAIRED) { onStatusChange(RepairStatus.REPAIRED) }
                }
            }
        }
    }
}

@Composable
fun StatusButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, fontSize = 10.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isSelected) Color(0xFFA594F9) else Color.Transparent,
            labelColor = if (isSelected) Color.White else Color.Gray
        )
    )
}

@Composable
fun StatusBadge(status: RepairStatus) {
    val (color, bgColor) = when (status) {
        RepairStatus.PENDING -> Color.Gray to Color.LightGray.copy(alpha = 0.3f)
        RepairStatus.REPAIRING -> Color(0xFF2196F3) to Color(0xFFE3F2FD)
        RepairStatus.REPAIRED -> Color(0xFF4CAF50) to Color(0xFFE8F5E9)
        RepairStatus.RELEASED -> Color(0xFF9C27B0) to Color(0xFFF3E5F5)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F0FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("New Customer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA594F9))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                
                HorizontalDivider(color = Color(0xFFA594F9).copy(alpha = 0.1f))
                
                StyledTextField(value = name, onValueChange = { name = it }, label = "Name")
                StyledTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
                StyledTextField(value = email, onValueChange = { email = it }, label = "Email")
                
                Button(
                    onClick = { onConfirm(name, phone, email) }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA594F9))
                ) {
                    Text("Register Customer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddRepairJobDialog(
    customers: List<Customer>,
    availableProducts: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (RepairJob, List<Product>) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var deviceModel by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf("") }
    var laborCost by remember { mutableStateOf("") }
    val selectedParts = remember { mutableStateListOf<Product>() }
    var partsSearchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(availableProducts, partsSearchQuery) {
        availableProducts.filter { 
            it.quantity > 0 && it.name.contains(partsSearchQuery, ignoreCase = true)
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
            shape = RoundedCornerShape(28.dp),
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("New Repair Job", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA594F9))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color(0xFFA594F9).copy(alpha = 0.1f))
                
                Text("Select Customer", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(customers) { customer ->
                        FilterChip(
                            selected = selectedCustomer == customer,
                            onClick = { selectedCustomer = customer },
                            label = { Text(customer.name) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFA594F9),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            ),
                            border = null
                        )
                    }
                }

                StyledTextField(value = deviceModel, onValueChange = { deviceModel = it }, label = "Device Model")
                StyledTextField(value = issue, onValueChange = { issue = it }, label = "Issue Description")
                StyledTextField(value = laborCost, onValueChange = { laborCost = it }, label = "Labor Cost")

                Spacer(Modifier.height(8.dp))
                Text("Parts Used", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                
                OutlinedTextField(
                    value = partsSearchQuery,
                    onValueChange = { partsSearchQuery = it },
                    placeholder = { Text("Search inventory...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFA594F9),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredProducts.forEach { product ->
                        val isSelected = selectedParts.contains(product)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isSelected) selectedParts.remove(product) 
                                    else selectedParts.add(product)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFA594F9)) else null
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF8F9FA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (product.imageUri != null) {
                                        AsyncImage(model = product.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text("📦", fontSize = 18.sp)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Stock: ${product.quantity} • ₱ ${product.price}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFA594F9))
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedCustomer?.let {
                            onConfirm(
                                RepairJob(
                                    customerId = it.id,
                                    deviceModel = deviceModel,
                                    issueDescription = issue,
                                    laborCost = laborCost.toDoubleOrNull() ?: 0.0
                                ),
                                selectedParts.toList()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA594F9)),
                    enabled = selectedCustomer != null && deviceModel.isNotBlank()
                ) {
                    Text("Save Repair Job", fontWeight = FontWeight.Bold)
                }
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