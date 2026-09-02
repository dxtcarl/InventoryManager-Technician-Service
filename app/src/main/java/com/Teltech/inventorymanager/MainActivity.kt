package com.Teltech.inventorymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.Teltech.inventorymanager.presentation.ai.AiScreen
import com.Teltech.inventorymanager.presentation.dashboard.DashboardScreen
import com.Teltech.inventorymanager.presentation.history.HistoryScreen
import com.Teltech.inventorymanager.presentation.products.ProductsScreen
import com.Teltech.inventorymanager.presentation.repairs.RepairsScreen
import com.Teltech.inventorymanager.presentation.settings.SettingsScreen
import com.Teltech.inventorymanager.presentation.welcome.WelcomeScreen
import com.Teltech.inventorymanager.presentation.theme.InventoryManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryManagerTheme {
                AppScaffold()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Welcome : Screen("welcome", "Welcome", Icons.Rounded.Celebration)
    object Dashboard : Screen("dashboard", "Home", Icons.Rounded.Home)
    object Products : Screen("products", "Inventory", Icons.Rounded.Inventory2)
    object Repairs : Screen("repairs", "Repairs", Icons.Rounded.Handyman)
    object Assistant : Screen("assistant", "AI Assistant", Icons.Rounded.AutoAwesome)
    object History : Screen("history", "History", Icons.Rounded.History)
    object Profile : Screen("settings", "Profile", Icons.Rounded.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val bottomItems = listOf(Screen.Dashboard, Screen.Products, Screen.Repairs)
    val drawerItems = listOf(Screen.Assistant, Screen.History, Screen.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBars = currentRoute != null && currentRoute != Screen.Welcome.route && currentRoute != "welcome"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showBars,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = Color.White
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Menu",
                            modifier = Modifier.padding(horizontal = 28.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA594F9)
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        drawerItems.forEach { screen ->
                            NavigationDrawerItem(
                                label = { Text(screen.label, fontWeight = FontWeight.Medium) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination()!!.id)
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = null) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color(0xFFF3F0FF),
                                    selectedIconColor = Color(0xFFA594F9),
                                    selectedTextColor = Color(0xFFA594F9),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Black
                                )
                            )
                        }
                        
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Teltech v1.0",
                            modifier = Modifier.padding(28.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    bottomBar = {
                        if (showBars) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                bottomItems.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination()!!.id)
                                                launchSingleTop = true
                                            }
                                        },
                                        icon = {
                                            Surface(
                                                color = if (selected) Color(0xFFF3F0FF) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.label,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).size(24.dp)
                                                )
                                            }
                                        },
                                        label = { Text(screen.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFFA594F9),
                                            unselectedIconColor = Color.Gray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Welcome.route,
                        modifier = Modifier.padding(if (showBars) padding else PaddingValues(0.dp))
                    ) {
                        composable(Screen.Welcome.route) {
                            WelcomeScreen(onGetStarted = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Dashboard.route) { 
                            DashboardScreen(onMenuClick = { scope.launch { drawerState.open() } }) 
                        }
                        composable(Screen.Products.route) { ProductsScreen() }
                        composable(Screen.Repairs.route) { RepairsScreen() }
                        composable(Screen.Assistant.route) { AiScreen() }
                        composable(Screen.History.route) { HistoryScreen() }
                        composable(Screen.Profile.route) { SettingsScreen() }
                    }
                }
            }
        }
    }
}
