package com.example.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BoxDistribution
import com.example.data.model.ItemWithDetails
import com.example.ui.components.AdjustQuantityDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MoveQuantityDialog
import com.example.ui.components.TagChip
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.AppGradients
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    viewModel: ItemsViewModel,
    onNavigateToItemDetail: (itemId: Long) -> Unit,
    onNavigateToItemAdd: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val allBoxes by viewModel.allBoxes.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedBoxId by viewModel.selectedBoxId.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val currentTheme by ThemeManager.currentTheme.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    // Dialog state for quick action from item card
    var moveDialogItem by remember { mutableStateOf<ItemWithDetails?>(null) }
    var adjustDialogItem by remember { mutableStateOf<Pair<ItemWithDetails, BoxDistribution>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.backgroundBrush),
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 3.dp)
                    .background(AppGradients.topBarBrush)
                    .border(
                        width = 0.5.dp,
                        brush = AppGradients.cardBorderBrush,
                        shape = RectangleShape
                    )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Objets (${filteredItems.size})",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier.testTag("items_theme_button")
                        ) {
                            val themeIcon = when (currentTheme) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.EMERALD -> Icons.Default.Forest
                                AppThemeMode.TERRACOTTA -> Icons.Default.LocalFireDepartment
                            }
                            Icon(
                                imageVector = themeIcon,
                                contentDescription = "Changer le thème (${currentTheme.displayName})"
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { isSortMenuExpanded = true },
                                modifier = Modifier.testTag("items_sort_button")
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Trier")
                            }
                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                ItemSortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.displayName,
                                                fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                                color = if (option == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            isSortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = AppGradients.reliefGlowColor)
                    .clip(CircleShape)
                    .background(AppGradients.primaryButtonBrush)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable { onNavigateToItemAdd() }
                    .padding(16.dp)
                    .testTag("add_item_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un objet",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("items_search_input"),
                placeholder = { Text("Rechercher un objet, tag, boîte...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter Chips Horizontal Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("items_filter_row")
            ) {
                // Clear all filters if active
                if (selectedCategory != null || selectedTagId != null || selectedBoxId != null || searchQuery.isNotEmpty()) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.clearFilters() },
                            label = { Text("Effacer filtres", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }

                // Categories
                categories.forEach { cat ->
                    item {
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Tags
                allTags.forEach { tag ->
                    item {
                        TagChip(
                            tag = tag,
                            isSelected = selectedTagId == tag.id,
                            onClick = { viewModel.setSelectedTagId(tag.id) }
                        )
                    }
                }

                // Boxes
                allBoxes.forEach { box ->
                    item {
                        FilterChip(
                            selected = selectedBoxId == box.id,
                            onClick = { viewModel.setSelectedBoxId(box.id) },
                            label = { Text(box.code, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items List or Empty State
            if (filteredItems.isEmpty()) {
                val isFiltered = searchQuery.isNotEmpty() || selectedCategory != null || selectedTagId != null || selectedBoxId != null
                EmptyStateView(
                    icon = Icons.Outlined.Inventory2,
                    title = if (isFiltered) "Aucun résultat" else "Aucun objet",
                    subtitle = if (isFiltered) "Aucun objet ne correspond à vos critères de recherche." else "Vous n’avez encore aucun objet dans votre inventaire.",
                    actionButtonText = if (isFiltered) "Réinitialiser les filtres" else "+ Ajouter mon premier objet",
                    onActionClick = {
                        if (isFiltered) viewModel.clearFilters() else onNavigateToItemAdd()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "items_empty_state"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("items_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.item.id }) { itemWithDetails ->
                        ItemCard(
                            itemWithDetails = itemWithDetails,
                            onClick = { onNavigateToItemDetail(itemWithDetails.item.id) },
                            onMoveClick = {
                                if (itemWithDetails.boxDistributions.isNotEmpty()) {
                                    moveDialogItem = itemWithDetails
                                }
                            },
                            onAdjustClick = {
                                if (itemWithDetails.boxDistributions.isNotEmpty()) {
                                    adjustDialogItem = Pair(itemWithDetails, itemWithDetails.boxDistributions.first())
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Move Quantity Dialog
    moveDialogItem?.let { itemWithDetails ->
        val firstDist = itemWithDetails.boxDistributions.firstOrNull()
        if (firstDist != null) {
            MoveQuantityDialog(
                itemName = itemWithDetails.item.name,
                sourceBox = firstDist.box,
                availableQuantity = firstDist.quantity,
                allBoxes = allBoxes,
                onConfirmMove = { toBoxId, qty ->
                    viewModel.moveQuantity(itemWithDetails.item.id, firstDist.box.id, toBoxId, qty)
                    moveDialogItem = null
                },
                onDismiss = { moveDialogItem = null }
            )
        }
    }

    // Adjust Quantity Dialog
    adjustDialogItem?.let { (itemWithDetails, dist) ->
        AdjustQuantityDialog(
            itemName = itemWithDetails.item.name,
            boxName = dist.box.name,
            currentQuantity = dist.quantity,
            onConfirmQuantity = { newQty ->
                viewModel.adjustQuantity(itemWithDetails.item.id, dist.box.id, newQty, dist.quantity)
                adjustDialogItem = null
            },
            onDismiss = { adjustDialogItem = null }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onSelectTheme = { selectedMode ->
                ThemeManager.setTheme(context, selectedMode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun ItemCard(
    itemWithDetails: ItemWithDetails,
    onClick: () -> Unit,
    onMoveClick: () -> Unit,
    onAdjustClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = AppGradients.reliefGlowColor)
            .clickable { onClick() }
            .testTag("item_card_${itemWithDetails.item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, brush = AppGradients.cardBorderBrush)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Photo or icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUri = itemWithDetails.item.primaryPhotoUri
                    if (photoUri != null && File(photoUri).exists()) {
                        AsyncImage(
                            model = File(photoUri),
                            contentDescription = itemWithDetails.item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemWithDetails.item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!itemWithDetails.item.category.isNullOrBlank()) {
                        Text(
                            text = itemWithDetails.item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    if (!itemWithDetails.item.description.isNullOrBlank()) {
                        Text(
                            text = itemWithDetails.item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quantity badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.testTag("item_qty_badge_${itemWithDetails.item.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${itemWithDetails.totalQuantity}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (itemWithDetails.totalQuantity > 1) "exemplaires" else "exemplaire",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Tags row
            if (itemWithDetails.tags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    items(itemWithDetails.tags) { tag ->
                        TagChip(tag = tag)
                    }
                }
            }

            // Storage breakdown
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    if (itemWithDetails.boxDistributions.isEmpty()) {
                        Text(
                            text = "Non rangé dans une boîte",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        itemWithDetails.boxDistributions.forEach { dist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${dist.box.name} (${dist.locationPath})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${dist.quantity} ex.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick actions buttons
            if (itemWithDetails.boxDistributions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onMoveClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("quick_move_btn_${itemWithDetails.item.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Déplacer", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onAdjustClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("quick_adjust_btn_${itemWithDetails.item.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quantité", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }
                }
            }
        }
    }
}
