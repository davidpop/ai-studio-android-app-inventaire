package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BoxWithDetails
import com.example.data.model.ItemWithDetails
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.TagChip
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.AppGradients
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToItemDetail: (itemId: Long) -> Unit,
    onNavigateToItemAdd: () -> Unit,
    onNavigateToBoxDetail: (boxId: Long) -> Unit,
    onNavigateToBoxAdd: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToLocations: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val stats by viewModel.stats.collectAsState()
    val recentItems by viewModel.recentItems.collectAsState()
    val recentBoxes by viewModel.recentBoxes.collectAsState()
    val currentTheme by ThemeManager.currentTheme.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonString by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputJson by remember { mutableStateOf("") }

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
                        Column {
                            Text(
                                text = "Inventaire",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Gestion d'inventaire personnel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier.testTag("dashboard_theme_button")
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
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier.testTag("dashboard_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Recherche")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                    .testTag("dashboard_add_item_fab"),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("dashboard_scroll_column"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            // Hero Banner Card with Theme Gradient Relief
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = AppGradients.reliefGlowColor)
                        .testTag("dashboard_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppGradients.heroBrush)
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Aperçu Global",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = currentTheme.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Espace Rangement & Stocks",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${stats.totalItems} articles inventoriés dans ${stats.totalBoxes} boîtes réparties sur ${stats.totalLocations} emplacements.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToSearch,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                        .testTag("hero_quick_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rechercher",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onNavigateToItemAdd,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("hero_quick_add_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ajouter objet",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Stats Overview Grid
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Objets",
                            value = "${stats.totalItems}",
                            subtitle = "${stats.totalQuantity} exemplaires au total",
                            icon = Icons.Outlined.Inventory2,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_items_card"
                        )
                        StatCard(
                            title = "Boîtes",
                            value = "${stats.totalBoxes}",
                            subtitle = "Unités de stockage",
                            icon = Icons.Default.Archive,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            testTag = "stat_boxes_card"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Localisations",
                            value = "${stats.totalLocations}",
                            subtitle = "Emplacements et pièces",
                            icon = Icons.Outlined.Place,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToLocations,
                            testTag = "stat_locations_card"
                        )
                        StatCard(
                            title = "Tags",
                            value = "${stats.totalTags}",
                            subtitle = "Catégories & étiquettes",
                            icon = Icons.Outlined.LocalOffer,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToTags,
                            testTag = "stat_tags_card"
                        )
                    }
                }
            }

            // 2. Quick Action Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = AppGradients.reliefGlowColor),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(width = 1.dp, brush = AppGradients.cardBorderBrush)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Actions rapides",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToItemAdd,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_add_item"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Objet", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = onNavigateToBoxAdd,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("quick_add_box"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Boîte", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 3. Recent Items
            item {
                SectionHeader(
                    title = "Objets récents",
                    subtitle = "Derniers objets modifiés ou ajoutés",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            if (recentItems.isEmpty()) {
                item {
                    Text(
                        text = "Aucun objet pour le moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(recentItems, key = { "item_${it.item.id}" }) { itemWithDetails ->
                    RecentItemCard(
                        itemWithDetails = itemWithDetails,
                        onClick = { onNavigateToItemDetail(itemWithDetails.item.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 4. Storage Boxes
            item {
                SectionHeader(
                    title = "Boîtes de rangement",
                    subtitle = "Contenants et emplacements",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            if (recentBoxes.isEmpty()) {
                item {
                    Text(
                        text = "Aucune boîte enregistrée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(recentBoxes, key = { "box_${it.box.id}" }) { boxWithDetails ->
                    RecentBoxCard(
                        boxWithDetails = boxWithDetails,
                        onClick = { onNavigateToBoxDetail(boxWithDetails.box.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 5. Data Management (Seeding & Backup)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = AppGradients.reliefGlowColor),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(width = 1.dp, brush = AppGradients.cardBorderBrush)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gestion des données & Sauvegarde",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exporter ou restaurer l'inventaire en JSON, ou réinitialiser les données de démo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.exportBackup { json ->
                                        exportJsonString = json
                                        showExportDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exporter", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    importInputJson = ""
                                    showImportDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_backup_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Importer", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { showResetConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_demo_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Démo", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showThemeDialog = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.primaryPreviewColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentTheme) {
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.EMERALD -> Icons.Default.Forest
                                        AppThemeMode.TERRACOTTA -> Icons.Default.LocalFireDepartment
                                    },
                                    contentDescription = null,
                                    tint = currentTheme.primaryPreviewColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Thème d'affichage",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "${currentTheme.displayName} • ${currentTheme.subtitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { showThemeDialog = true },
                                modifier = Modifier.testTag("dashboard_change_theme_card_button")
                            ) {
                                Text("Changer")
                            }
                        }
                    }
                }
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onSelectTheme = { selectedMode ->
                ThemeManager.setTheme(context, selectedMode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Reset Demo Confirmation Dialog
    if (showResetConfirm) {
        ConfirmDeleteDialog(
            title = "Réinitialiser les données",
            message = "Cette action réinitialisera l'inventaire avec les données de démonstration (Maison, Garage, Tournevis, Câbles USB-C...). Vos données actuelles seront remplacées.",
            confirmText = "Réinitialiser",
            onConfirm = {
                viewModel.resetDemoData()
                showResetConfirm = false
            },
            onDismiss = { showResetConfirm = false }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Sauvegarde générée (JSON)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Le contenu JSON complet de votre inventaire a été généré. Vous pouvez le copier pour le sauvegarder.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = exportJsonString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("export_json_textfield"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportJsonString))
                        Toast.makeText(context, "JSON copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Copier")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExportDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restaurer une sauvegarde", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Collez le texte JSON d'une sauvegarde précédente. Attention : cela remplacera les données actuelles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importInputJson,
                        onValueChange = { importInputJson = it },
                        placeholder = { Text("Collez le code JSON ici...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("import_json_textfield"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importBackup(importInputJson)
                        showImportDialog = false
                    },
                    enabled = importInputJson.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Restaurer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showImportDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.25f)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.85f),
                    color.copy(alpha = 0.25f),
                    color.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            color.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(elevation = 2.dp, shape = CircleShape, spotColor = color.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        color.copy(alpha = 0.25f),
                                        color.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentItemCard(
    itemWithDetails: ItemWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), spotColor = AppGradients.reliefGlowColor)
            .clickable { onClick() }
            .testTag("recent_item_${itemWithDetails.item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, brush = AppGradients.cardBorderBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail or placeholder
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemWithDetails.item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!itemWithDetails.item.category.isNullOrBlank()) {
                    Text(
                        text = itemWithDetails.item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (itemWithDetails.tags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(itemWithDetails.tags.take(3)) { tag ->
                            TagChip(tag = tag)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Total quantity badge with relief
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.testTag("recent_item_qty_${itemWithDetails.item.id}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${itemWithDetails.totalQuantity}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "ex.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentBoxCard(
    boxWithDetails: BoxWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp), spotColor = AppGradients.reliefGlowColor)
            .clickable { onClick() }
            .testTag("recent_box_${boxWithDetails.box.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, brush = AppGradients.cardBorderBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = boxWithDetails.box.code,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = boxWithDetails.box.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = boxWithDetails.locationPath,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${boxWithDetails.itemCount} objets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
