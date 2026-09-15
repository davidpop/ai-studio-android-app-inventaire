package com.example.ui.boxes

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ItemInBox
import com.example.data.model.StorageBox
import com.example.ui.components.AdjustQuantityDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.MoveQuantityDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    boxId: Long,
    viewModel: BoxDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (boxId: Long) -> Unit,
    onNavigateToCamera: (boxId: Long) -> Unit,
    onNavigateToItemDetail: (itemId: Long) -> Unit
) {
    val context = LocalContext.current
    val boxWithDetails by viewModel.boxWithDetails.collectAsState()
    val allBoxes by viewModel.allBoxes.collectAsState()
    val allLocations by viewModel.allLocations.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReassignDeleteDialog by remember { mutableStateOf(false) }
    var reassignTargetBoxId by remember { mutableStateOf<Long?>(null) }
    var showRelocateDialog by remember { mutableStateOf(false) }

    var moveDialogItem by remember { mutableStateOf<ItemInBox?>(null) }
    var adjustDialogItem by remember { mutableStateOf<ItemInBox?>(null) }

    LaunchedEffect(boxId) {
        viewModel.setBoxId(boxId)
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val boxDetails = boxWithDetails

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = boxDetails?.let { "${it.box.code} — ${it.box.name}" } ?: "Boîte",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("box_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (boxDetails != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(boxDetails.box.id) },
                            modifier = Modifier.testTag("box_detail_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                        IconButton(
                            onClick = {
                                if (boxDetails.items.isNotEmpty()) {
                                    showReassignDeleteDialog = true
                                } else {
                                    showDeleteConfirm = true
                                }
                            },
                            modifier = Modifier.testTag("box_detail_delete_btn")
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
        if (boxDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Chargement de la boîte...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("box_detail_scroll"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Box Photo or Hero Banner
                item {
                    val photoUri = boxDetails.box.photoUri
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .testTag("box_detail_photo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null && File(photoUri).exists()) {
                            AsyncImage(
                                model = File(photoUri),
                                contentDescription = boxDetails.box.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onNavigateToCamera(boxDetails.box.id) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prendre en photo la boîte")
                                }
                            }
                        }
                    }
                }

                // 2. Box Details Card
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
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = boxDetails.box.code,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = boxDetails.box.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (!boxDetails.box.label.isNullOrBlank() && boxDetails.box.label != boxDetails.box.name) {
                                        Text(
                                            text = "Étiquette : ${boxDetails.box.label}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${boxDetails.itemCount}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "objets (${boxDetails.totalQuantity} ex.)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            if (!boxDetails.box.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = boxDetails.box.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Location with relocate button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "Emplacement",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = boxDetails.locationPath,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showRelocateDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("box_relocate_btn")
                                    ) {
                                        Text("Déplacer boîte", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Items inside this box
                item {
                    Text(
                        text = "Objets rangés dans cette boîte (${boxDetails.items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (boxDetails.items.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Cette boîte est vide. Ajoutez des objets en créant ou modifiant un objet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(boxDetails.items, key = { it.item.id }) { boxItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToItemDetail(boxItem.item.id) }
                                .testTag("box_item_${boxItem.item.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val photoUri = boxItem.item.primaryPhotoUri
                                        if (photoUri != null && File(photoUri).exists()) {
                                            AsyncImage(
                                                model = File(photoUri),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.Inventory2,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = boxItem.item.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!boxItem.item.category.isNullOrBlank()) {
                                            Text(
                                                text = boxItem.item.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${boxItem.quantity} ex.",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { moveDialogItem = boxItem },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("box_item_move_${boxItem.item.id}")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Déplacer", style = MaterialTheme.typography.labelSmall)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = { adjustDialogItem = boxItem },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("box_item_adjust_${boxItem.item.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quantité", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Relocate Box Dialog
    if (showRelocateDialog) {
        var selectedLocation by remember { mutableStateOf(boxDetails?.box?.locationId) }
        AlertDialog(
            onDismissRequest = { showRelocateDialog = false },
            title = { Text("Changer l'emplacement de la boîte") },
            text = {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLocation = null }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aucun emplacement (non assigné)",
                                color = if (selectedLocation == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedLocation == null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    items(allLocations) { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLocation = loc.id }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = loc.name,
                                color = if (selectedLocation == loc.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedLocation == loc.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateLocation(selectedLocation)
                    showRelocateDialog = false
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRelocateDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Move item dialog
    moveDialogItem?.let { boxItem ->
        boxDetails?.let { b ->
            MoveQuantityDialog(
                itemName = boxItem.item.name,
                sourceBox = b.box,
                availableQuantity = boxItem.quantity,
                allBoxes = allBoxes,
                onConfirmMove = { toBoxId, qty ->
                    viewModel.moveItemQuantity(boxItem.item.id, toBoxId, qty)
                    moveDialogItem = null
                },
                onDismiss = { moveDialogItem = null }
            )
        }
    }

    // Adjust item quantity dialog
    adjustDialogItem?.let { boxItem ->
        boxDetails?.let { b ->
            AdjustQuantityDialog(
                itemName = boxItem.item.name,
                boxName = b.box.name,
                currentQuantity = boxItem.quantity,
                onConfirmQuantity = { newQty ->
                    viewModel.adjustItemQuantity(boxItem.item.id, newQty, boxItem.quantity)
                    adjustDialogItem = null
                },
                onDismiss = { adjustDialogItem = null }
            )
        }
    }

    // Simple delete confirm if box is empty
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Supprimer la boîte",
            message = "Voulez-vous vraiment supprimer cette boîte ? Cette action est irréversible.",
            onConfirm = {
                viewModel.deleteBox {
                    showDeleteConfirm = false
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // Reassign and Delete Dialog if box contains items!
    if (showReassignDeleteDialog) {
        val targetCandidates = allBoxes.filter { it.id != boxId }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showReassignDeleteDialog = false },
            title = { Text("La boîte contient des objets !", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Cette boîte contient ${boxDetails?.items?.size} objet(s). Pour éviter de perdre l'inventaire, vous pouvez transférer tous ces objets vers une autre boîte avant de la supprimer.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (targetCandidates.isEmpty()) {
                        Text(
                            text = "Aucune autre boîte disponible vers laquelle transférer les objets.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "Boîte de destination :",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it }
                        ) {
                            val selectedName = targetCandidates.find { it.id == reassignTargetBoxId }?.let { "${it.code} - ${it.name}" }
                                ?: "Sélectionner une boîte de secours..."
                            OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                targetCandidates.forEach { candidate ->
                                    DropdownMenuItem(
                                        text = { Text("${candidate.code} — ${candidate.name}") },
                                        onClick = {
                                            reassignTargetBoxId = candidate.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (targetCandidates.isNotEmpty()) {
                    Button(
                        onClick = {
                            val target = reassignTargetBoxId
                            if (target != null) {
                                viewModel.reassignAndDeleteBox(target) {
                                    showReassignDeleteDialog = false
                                    onNavigateBack()
                                }
                            }
                        },
                        enabled = reassignTargetBoxId != null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Transférer et supprimer")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.deleteBox {
                                showReassignDeleteDialog = false
                                onNavigateBack()
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Supprimer quand même")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showReassignDeleteDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}
