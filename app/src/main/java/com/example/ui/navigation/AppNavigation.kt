package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.AppGradients
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.boxes.BoxDetailScreen
import com.example.ui.boxes.BoxDetailViewModel
import com.example.ui.boxes.BoxEditScreen
import com.example.ui.boxes.BoxEditViewModel
import com.example.ui.boxes.BoxesListScreen
import com.example.ui.boxes.BoxesViewModel
import com.example.ui.camera.CameraCaptureScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.items.ItemDetailScreen
import com.example.ui.items.ItemDetailViewModel
import com.example.ui.items.ItemEditScreen
import com.example.ui.items.ItemEditViewModel
import com.example.ui.items.ItemsListScreen
import com.example.ui.items.ItemsViewModel
import com.example.ui.locations.LocationsScreen
import com.example.ui.locations.LocationsViewModel
import com.example.ui.search.GlobalSearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.tags.TagsScreen
import com.example.ui.tags.TagsViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    val testTag: String = ""
) {
    object Dashboard : Screen("dashboard", "Accueil", Icons.Default.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard")
    object Items : Screen("items", "Objets", Icons.Outlined.Inventory2, Icons.Outlined.Inventory2, "nav_items")
    object Boxes : Screen("boxes", "Boîtes", Icons.Default.Archive, Icons.Outlined.Archive, "nav_boxes")
    object Locations : Screen("locations", "Lieux", Icons.Default.Place, Icons.Outlined.Place, "nav_locations")

    object ItemDetail : Screen("item_detail/{itemId}", "Détail Objet") {
        fun createRoute(itemId: Long) = "item_detail/$itemId"
    }

    object ItemEdit : Screen("item_edit?itemId={itemId}", "Modifier Objet") {
        fun createRoute(itemId: Long? = null) = if (itemId != null) "item_edit?itemId=$itemId" else "item_edit"
    }

    object BoxDetail : Screen("box_detail/{boxId}", "Détail Boîte") {
        fun createRoute(boxId: Long) = "box_detail/$boxId"
    }

    object BoxEdit : Screen("box_edit?boxId={boxId}", "Modifier Boîte") {
        fun createRoute(boxId: Long? = null) = if (boxId != null) "box_edit?boxId=$boxId" else "box_edit"
    }

    object Tags : Screen("tags", "Tags")
    object Search : Screen("search", "Recherche")
    object Camera : Screen("camera?targetType={targetType}&targetId={targetId}", "Caméra") {
        fun createRoute(targetType: String, targetId: Long = 0L) = "camera?targetType=$targetType&targetId=$targetId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Items,
    Screen.Boxes,
    Screen.Locations
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on root tabs
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.backgroundBrush),
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 8.dp)
                        .background(AppGradients.surfaceGradientBrush)
                        .border(
                            width = 1.dp,
                            brush = AppGradients.cardBorderBrush,
                            shape = RectangleShape
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.testTag("app_bottom_bar")
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            // 1. Dashboard
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToItemDetail = { itemId ->
                        navController.navigate(Screen.ItemDetail.createRoute(itemId))
                    },
                    onNavigateToItemAdd = {
                        navController.navigate(Screen.ItemEdit.createRoute())
                    },
                    onNavigateToBoxDetail = { boxId ->
                        navController.navigate(Screen.BoxDetail.createRoute(boxId))
                    },
                    onNavigateToBoxAdd = {
                        navController.navigate(Screen.BoxEdit.createRoute())
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToTags = {
                        navController.navigate(Screen.Tags.route)
                    },
                    onNavigateToLocations = {
                        navController.navigate(Screen.Locations.route)
                    }
                )
            }

            // 2. Items List
            composable(Screen.Items.route) {
                val vm: ItemsViewModel = viewModel()
                ItemsListScreen(
                    viewModel = vm,
                    onNavigateToItemDetail = { itemId ->
                        navController.navigate(Screen.ItemDetail.createRoute(itemId))
                    },
                    onNavigateToItemAdd = {
                        navController.navigate(Screen.ItemEdit.createRoute())
                    }
                )
            }

            // 3. Item Detail
            composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                val vm: ItemDetailViewModel = viewModel()
                ItemDetailScreen(
                    itemId = itemId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.ItemEdit.createRoute(id))
                    },
                    onNavigateToCamera = { targetId ->
                        navController.navigate(Screen.Camera.createRoute(targetType = "item", targetId = targetId))
                    },
                    onNavigateToBoxDetail = { bId ->
                        navController.navigate(Screen.BoxDetail.createRoute(bId))
                    }
                )
            }

            // 4. Item Edit / Add
            composable(
                route = Screen.ItemEdit.route,
                arguments = listOf(
                    navArgument("itemId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val itemIdArg = backStackEntry.arguments?.getLong("itemId")
                val itemId = if (itemIdArg != null && itemIdArg > 0) itemIdArg else null
                val vm: ItemEditViewModel = viewModel()
                ItemEditScreen(
                    itemId = itemId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { savedId ->
                        navController.popBackStack()
                        navController.navigate(Screen.ItemDetail.createRoute(savedId))
                    },
                    onNavigateToCamera = {
                        navController.navigate(Screen.Camera.createRoute(targetType = "item_edit", targetId = itemId ?: 0L))
                    }
                )
            }

            // 5. Boxes List
            composable(Screen.Boxes.route) {
                val vm: BoxesViewModel = viewModel()
                BoxesListScreen(
                    viewModel = vm,
                    onNavigateToBoxDetail = { boxId ->
                        navController.navigate(Screen.BoxDetail.createRoute(boxId))
                    },
                    onNavigateToBoxAdd = {
                        navController.navigate(Screen.BoxEdit.createRoute())
                    }
                )
            }

            // 6. Box Detail
            composable(
                route = Screen.BoxDetail.route,
                arguments = listOf(navArgument("boxId") { type = NavType.LongType })
            ) { backStackEntry ->
                val boxId = backStackEntry.arguments?.getLong("boxId") ?: 0L
                val vm: BoxDetailViewModel = viewModel()
                BoxDetailScreen(
                    boxId = boxId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { bId ->
                        navController.navigate(Screen.BoxEdit.createRoute(bId))
                    },
                    onNavigateToCamera = { bId ->
                        navController.navigate(Screen.Camera.createRoute(targetType = "box", targetId = bId))
                    },
                    onNavigateToItemDetail = { iId ->
                        navController.navigate(Screen.ItemDetail.createRoute(iId))
                    }
                )
            }

            // 7. Box Edit / Add
            composable(
                route = Screen.BoxEdit.route,
                arguments = listOf(
                    navArgument("boxId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val boxIdArg = backStackEntry.arguments?.getLong("boxId")
                val boxId = if (boxIdArg != null && boxIdArg > 0) boxIdArg else null
                val vm: BoxEditViewModel = viewModel()
                BoxEditScreen(
                    boxId = boxId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { savedId ->
                        navController.popBackStack()
                        navController.navigate(Screen.BoxDetail.createRoute(savedId))
                    }
                )
            }

            // 8. Locations
            composable(Screen.Locations.route) {
                val vm: LocationsViewModel = viewModel()
                LocationsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 9. Tags
            composable(Screen.Tags.route) {
                val vm: TagsViewModel = viewModel()
                TagsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 10. Global Search
            composable(Screen.Search.route) {
                val vm: SearchViewModel = viewModel()
                GlobalSearchScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToItemDetail = { itemId ->
                        navController.navigate(Screen.ItemDetail.createRoute(itemId))
                    },
                    onNavigateToBoxDetail = { boxId ->
                        navController.navigate(Screen.BoxDetail.createRoute(boxId))
                    },
                    onNavigateToLocations = {
                        navController.navigate(Screen.Locations.route)
                    },
                    onNavigateToTags = {
                        navController.navigate(Screen.Tags.route)
                    }
                )
            }

            // 11. Camera
            composable(
                route = Screen.Camera.route,
                arguments = listOf(
                    navArgument("targetType") {
                        type = NavType.StringType
                        defaultValue = "general"
                    },
                    navArgument("targetId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val targetType = backStackEntry.arguments?.getString("targetType") ?: "general"
                val targetId = backStackEntry.arguments?.getLong("targetId") ?: 0L

                CameraCaptureScreen(
                    onPhotoCaptured = { photoPath ->
                        if (targetType == "item" && targetId > 0) {
                            // Handled by item detail view model or backstack
                        }
                        navController.popBackStack()
                    },
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
