package com.Teltech.inventorymanager.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars

@Composable
fun DashboardScreen(
    onMenuClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    
    val growth = data.stockGrowthPercentage
    val growthText = if (growth >= 0) {
        "+%.1f%% Rise".format(growth) 
    } else {
        "%.1f%% Fall".format(growth)
    }
    val growthColor = if (growth >= 0) Color(0xFF4CAF50) else Color.Red

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    if (profile?.profilePictureUri != null) {
                        AsyncImage(
                            model = profile?.profilePictureUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("👨‍💼", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(" HELLO ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${profile?.name ?: "TELTECH"} ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Top Cards
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard(
                title = "Total Value",
                value = "₱ %.0f".format(data.totalStockValue),
                icon = Icons.Rounded.AccountBalanceWallet,
                containerColor = Color(0xFFA594F9),
                contentColor = Color.White,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Total Stock",
                value = "%,d".format(data.totalStockQuantity),
                icon = Icons.Rounded.Inventory2,
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        // Business Insights Section
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsCard(
                title = "Top Customers",
                items = data.topCustomers,
                icon = Icons.Rounded.Star,
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "Popular Parts",
                items = data.topParts,
                icon = Icons.Rounded.Extension,
                modifier = Modifier.weight(1f)
            )
        }

        // Middle Status Cards
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard(
                title = "Customers",
                value = "%02d".format(data.customerCount),
                icon = Icons.Rounded.People,
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Low Stock",
                value = "%02d".format(data.lowStockCount),
                icon = Icons.Rounded.WarningAmber,
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Stock Flow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text ="$growthText in Inventory",
                            color = growthColor,
                            fontSize = 12.sp
                        )
                    }
                    Box {
                        TextButton(onClick = { showMenu = true }) {
                            Text("Last 7 days ∨", color = Color.Gray, fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Last 7 days") }, onClick = { showMenu = false })
                            DropdownMenuItem(text = { Text("Last 30 days") }, onClick = { showMenu = false })
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                
                if (data.weeklyFlow.isNotEmpty()) {
                    val bars = data.weeklyFlow.mapIndexed { index, value ->
                        Bars(
                            label = data.flowLabels.getOrElse(index) { "" },
                            values = listOf(Bars.Data(value = value, color = SolidColor(Color(0xFFA594F9))))
                        )
                    }

                    ColumnChart(
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        data = bars,
                        barProperties = BarProperties(
                            spacing = 16.dp,
                            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 8.dp, topLeft = 8.dp)
                        )
                    )
                } else {
                    Box(Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No stock activity yet", color = Color.Gray)
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    items: List<Pair<String, Int>>,
    icon: ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(140.dp), // Fixed height to stay consistent
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFFA594F9), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Only show Top 2 items to keep the card size fixed
                    items.take(2).forEach { (name, count) ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, fontSize = 10.sp, color = Color.DarkGray, maxLines = 1, modifier = Modifier.weight(1f))
                                Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA594F9))
                            }
                            Spacer(Modifier.height(4.dp))
                            // Small progress bar to represent popularity
                            LinearProgressIndicator(
                                progress = { count.toFloat() / (items.firstOrNull()?.second ?: 1).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = Color(0xFFA594F9),
                                trackColor = Color(0xFFF3F0FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (containerColor == Color.White) Color(0xFFF3F0FF) else Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(18.dp),
                    tint = if (containerColor == Color.White) Color(0xFFA594F9) else Color.White
                )
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ArrowOutward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}