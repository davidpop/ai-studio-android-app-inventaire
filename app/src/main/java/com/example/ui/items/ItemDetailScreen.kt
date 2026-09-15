package com.example.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.ItemPhoto
import com.example.ui.components.AdjustQuantityDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.MoveQuantityDialog
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.TagChip
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    viewModel: ItemDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (itemId: Long) -> Unit,
    onNavigateToCamera: (targetId: Long) -> Unit,
    onNavigateToBoxDetail: (boxId: Long) -> Unit
) {
    val context = LocalContext.current
    val itemWithDetails by viewModel.itemWithDetails.collectAsState()
    val allBoxes by viewModel.allBoxes.collectAsState()
    val selectedPhotoForViewer by viewModel.selectedPhotoForViewer.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var moveDialogDist by remember { mutableStateOf<BoxDistribution?>(null) }
    var adjustDialogDist by remember { mutableStateOf<BoxDistribution?>(null) }

    LaunchedEffect(itemId) {
        viewModel.setItemId(itemId)
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val itemDetails = itemWithDetails

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemDetails?.item?.name ?: "Détail de l'objet",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("item_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (itemDetails != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(itemDetails.item.id) },
                            modifier = Modifier.testTag("item_detail_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("item_detail_delete_btn")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (itemDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chargement de l'objet...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("item_detail_scroll"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Primary Photo Hero Banner
                item {
                    val primaryPhoto = itemDetails.photos.find { it.isPrimary } ?: itemDetails.photos.firstOrNull()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = primaryPhoto != null) {
                                if (primaryPhoto != null) {
                                    viewModel.openPhotoViewer(primaryPhoto)
                                }
                            }
                            .testTag("item_detail_hero_photo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (primaryPhoto != null && File(primaryPhoto.photoPath).exists()) {
                            AsyncImage(
                                model = File(primaryPhoto.photoPath),
                                contentDescription = itemDetails.item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucune photo principale",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Identity Card (Title, Category, Description, Tags)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = itemDetails.item.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!itemDetails.item.category.isNullOrBlank()) {
                                        Text(
                                            text = itemDetails.item.category,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.testTag("item_detail_total_qty")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${itemDetails.totalQuantity}",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "exemplaires au total",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            if (!itemDetails.item.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = itemDetails.item.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (itemDetails.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(itemDetails.tags) { tag ->
                                        TagChip(tag = tag)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Storage Distribution Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Répartition par boîte (${itemDetails.boxDistributions.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (itemDetails.boxDistributions.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "Cet objet n'est assigné à aucune boîte.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemDetails.boxDistributions.forEach { dist ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.clickable { onNavigateToBoxDetail(dist.box.id) }
                                                    ) {
                                                        Text(
                                                            text = dist.box.code,
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.secondary
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = dist.box.name,
                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Place,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = dist.locationPath,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        text = "${dist.quantity} ex.",
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { moveDialogDist = dist },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("detail_move_btn_${dist.box.id}")
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.CompareArrows,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Déplacer des exemplaires")
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                OutlinedButton(
                                                    onClick = { adjustDialogDist = dist },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("detail_adjust_btn_${dist.box.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Modifier")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Photo Gallery Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photos (${itemDetails.photos.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { onNavigateToCamera(itemDetails.item.id) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("item_add_photo_btn")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ajouter photo")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (itemDetails.photos.isEmpty()) {
                            Text(
                                text = "Aucune photo associée à cet objet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(itemDetails.photos, key = { it.id }) { photo ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(
                                                width = if (photo.isPrimary) 2.dp else 1.dp,
                                                color = if (photo.isPrimary) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { viewModel.openPhotoViewer(photo) }
                                            .testTag("photo_thumb_${photo.id}")
                                    ) {
                                        AsyncImage(
                                            model = File(photo.photoPath),
                                            contentDescription = "Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (photo.isPrimary) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("★", color = Color(0xFFFCD34D), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Photo Viewer Fullscreen Dialog
    selectedPhotoForViewer?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            onDismiss = { viewModel.closePhotoViewer() },
            onSetPrimary = { viewModel.setPrimaryPhoto(photo) },
            onDelete = { viewModel.deletePhoto(photo) }
        )
    }

    // Move Quantity Dialog
    moveDialogDist?.let { dist ->
        itemDetails?.let { itm ->
            MoveQuantityDialog(
                itemName = itm.item.name,
                sourceBox = dist.box,
                availableQuantity = dist.quantity,
                allBoxes = allBoxes,
                onConfirmMove = { toBoxId, qty ->
                    viewModel.moveQuantity(dist.box.id, toBoxId, qty)
                    moveDialogDist = null
                },
                onDismiss = { moveDialogDist = null }
            )
        }
    }

    // Adjust Quantity Dialog
    adjustDialogDist?.let { dist ->
        itemDetails?.let { itm ->
            AdjustQuantityDialog(
                itemName = itm.item.name,
                boxName = dist.box.name,
                currentQuantity = dist.quantity,
                onConfirmQuantity = { newQty ->
                    viewModel.adjustQuantity(dist.box.id, newQty, dist.quantity)
                    adjustDialogDist = null
                },
                onDismiss = { adjustDialogDist = null }
            )
        }
    }

    // Confirm Delete Dialog
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Supprimer l'objet",
            message = "Voulez-vous vraiment supprimer cet objet et toutes ses photos associées ? Cette action est irréversible.",
            onConfirm = {
                viewModel.deleteItem {
                    showDeleteConfirm = false
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
