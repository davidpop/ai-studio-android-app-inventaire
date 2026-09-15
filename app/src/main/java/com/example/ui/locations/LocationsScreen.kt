package com.example.ui.locations

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Location
import com.example.data.model.LocationNode
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    viewModel: LocationsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val locationTree by viewModel.locationTree.collectAsState()
    val allLocations by viewModel.allLocations.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingLocation by remember { mutableStateOf<Location?>(null) }
    var locationNameInput by remember { mutableStateOf("") }
    var locationDescInput by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }

    var deletingLocation by remember { mutableStateOf<Location?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Localisations hiérarchiques",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("locations_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingLocation = null
                    locationNameInput = ""
                    locationDescInput = ""
                    selectedParentId = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.testTag("add_location_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un emplacement racine")
            }
        }
    ) { padding ->
        if (locationTree.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Place,
                title = "Aucun emplacement",
                subtitle = "Créez vos emplacements pour organiser vos boîtes de rangement (ex: Maison > Garage > Étagère 3).",
                actionButtonText = "+ Créer un emplacement racine",
                onActionClick = {
                    editingLocation = null
                    locationNameInput = ""
                    locationDescInput = ""
                    selectedParentId = null
                    showEditDialog = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                testTag = "locations_empty_state"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("locations_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(locationTree, key = { it.location.id }) { rootNode ->
                    LocationTreeNodeItem(
                        node = rootNode,
                        depth = 0,
                        onAddSubLocation = { parentId ->
                            editingLocation = null
                            locationNameInput = ""
                            locationDescInput = ""
                            selectedParentId = parentId
                            showEditDialog = true
                        },
                        onEditLocation = { loc ->
                            editingLocation = loc
                            locationNameInput = loc.name
                            locationDescInput = loc.description ?: ""
                            selectedParentId = loc.parentId
                            showEditDialog = true
                        },
                        onDeleteLocation = { loc ->
                            deletingLocation = loc
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Location Dialog
    if (showEditDialog) {
        var dropdownExpanded by remember { mutableStateOf(false) }
        val availableParents = allLocations.filter { loc ->
            editingLocation == null || loc.id != editingLocation?.id
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingLocation == null) {
                        if (selectedParentId == null) "Nouvel emplacement racine" else "Ajouter un sous-emplacement"
                    } else "Modifier l'emplacement",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = locationNameInput,
                        onValueChange = { locationNameInput = it },
                        label = { Text("Nom de l'emplacement *") },
                        placeholder = { Text("Ex: Maison, Garage, Étagère A...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = locationDescInput,
                        onValueChange = { locationDescInput = it },
                        label = { Text("Description ou précisions") },
                        placeholder = { Text("Ex: Deuxième porte à gauche...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parent selection dropdown
                    Text("Emplacement parent (optionnel) :", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    val currentParentName = availableParents.find { it.id == selectedParentId }?.name
                        ?: "Aucun (Racine)"

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentParentName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("parent_location_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Aucun (Emplacement racine)") },
                                onClick = {
                                    selectedParentId = null
                                    dropdownExpanded = false
                                }
                            )
                            availableParents.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = {
                                        selectedParentId = p.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveLocation(
                            id = editingLocation?.id ?: 0L,
                            name = locationNameInput,
                            parentId = selectedParentId,
                            description = locationDescInput
                        )
                        showEditDialog = false
                    },
                    enabled = locationNameInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_location_button")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    // Delete Confirmation
    deletingLocation?.let { loc ->
        ConfirmDeleteDialog(
            title = "Supprimer l'emplacement",
            message = "Voulez-vous supprimer '${loc.name}' ? Ses sous-emplacements seront rattachés au niveau supérieur.",
            onConfirm = {
                viewModel.deleteLocation(loc.id)
                deletingLocation = null
            },
            onDismiss = { deletingLocation = null }
        )
    }
}

@Composable
private fun LocationTreeNodeItem(
    node: LocationNode,
    depth: Int,
    onAddSubLocation: (parentId: Long) -> Unit,
    onEditLocation: (location: Location) -> Unit,
    onDeleteLocation: (location: Location) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 20).dp)
                .testTag("location_node_${node.location.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (depth == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (depth == 0) 1.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Folder icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (depth == 0) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (node.children.isNotEmpty()) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (depth == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.location.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!node.location.description.isNullOrBlank()) {
                        Text(
                            text = node.location.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add sub-location button
                    IconButton(
                        onClick = { onAddSubLocation(node.location.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter sous-emplacement",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = { onEditLocation(node.location) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = { onDeleteLocation(node.location) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Render children recursively
        if (isExpanded && node.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            node.children.forEach { childNode ->
                LocationTreeNodeItem(
                    node = childNode,
                    depth = depth + 1,
                    onAddSubLocation = onAddSubLocation,
                    onEditLocation = onEditLocation,
                    onDeleteLocation = onDeleteLocation
                )
            }
        }
    }
}
