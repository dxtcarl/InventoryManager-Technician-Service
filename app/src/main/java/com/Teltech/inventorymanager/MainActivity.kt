package com.yourname.inventorymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourname.inventorymanager.presentation.dashboard.DashboardScreen
import com.yourname.inventorymanager.presentation.products.ProductsScreen
import com.yourname.inventorymanager.presentation.theme.InventoryManagerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Dashboard : Screen("dashboard", "Dashboard", "📊")
    object Products : Screen("products", "Products", "📦")
}

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val items = listOf(Screen.Dashboard, Screen.Products)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination()!!.id)
                                launchSingleTop = true
                            }
                        },
                        icon = { Text(screen.icon) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Products.route) { ProductsScreen() }
        }
    }
}