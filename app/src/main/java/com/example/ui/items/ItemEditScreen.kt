package com.example.ui.items

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StorageBox
import com.example.ui.components.TagChip
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemEditScreen(
    itemId: Long?,
    viewModel: ItemEditViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (savedItemId: Long) -> Unit,
    onNavigateToCamera: () -> Unit
) {
    val context = LocalContext.current
    val name by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val category by viewModel.category.collectAsState()
    val selectedTagIds by viewModel.selectedTagIds.collectAsState()
    val boxQuantities by viewModel.boxQuantities.collectAsState()
    val pendingPhotos by viewModel.pendingPhotos.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val allBoxes by viewModel.allBoxes.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showNewTagDialog by remember { mutableStateOf(false) }
    var newTagNameInput by remember { mutableStateOf("") }
    var selectedTagColorHex by remember { mutableStateOf("#3B82F6") }

    var showAddBoxDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    LaunchedEffect(itemId) {
        viewModel.initForEdit(itemId)
    }

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
                        text = if (itemId == null || itemId <= 0) "Nouvel objet" else "Modifier l'objet",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("item_edit_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save(onSaveSuccess) },
                        enabled = !isSaving && name.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("item_edit_save_button")
                    ) {
                        Text("Enregistrer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("item_edit_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. General Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informations principales",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = { Text("Nom de l'objet *") },
                            placeholder = { Text("Ex: Câble USB-C, Tournevis...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category with dropdown and add new category
                        ExposedDropdownMenuBox(
                            expanded = showCategoryDropdown,
                            onExpandedChange = { showCategoryDropdown = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { viewModel.onCategoryChange(it) },
                                label = { Text("Catégorie") },
                                placeholder = { Text("Sélectionner ou saisir une catégorie...") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("item_category_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                allCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            viewModel.onCategoryChange(cat)
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Ajouter une nouvelle catégorie...",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showCategoryDropdown = false
                                        newCategoryInput = ""
                                        showAddCategoryDialog = true
                                    },
                                    modifier = Modifier.testTag("add_new_category_option")
                                )
                            }
                        }

                        if (showAddCategoryDialog) {
                            AlertDialog(
                                onDismissRequest = { showAddCategoryDialog = false },
                                title = { Text("Nouvelle catégorie") },
                                text = {
                                    OutlinedTextField(
                                        value = newCategoryInput,
                                        onValueChange = { newCategoryInput = it },
                                        label = { Text("Nom de la catégorie") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("new_category_dialog_input")
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val trimmed = newCategoryInput.trim()
                                            if (trimmed.isNotBlank()) {
                                                viewModel.onCategoryChange(trimmed)
                                            }
                                            showAddCategoryDialog = false
                                        },
                                        modifier = Modifier.testTag("new_category_dialog_confirm")
                                    ) {
                                        Text("Ajouter")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showAddCategoryDialog = false },
                                        modifier = Modifier.testTag("new_category_dialog_cancel")
                                    ) {
                                        Text("Annuler")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { viewModel.onDescriptionChange(it) },
                            label = { Text("Description ou notes") },
                            placeholder = { Text("Détails, références, caractéristiques...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_description_input"),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // 2. Tags Section
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
                            Text(
                                text = "Tags & Étiquettes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            OutlinedButton(
                                onClick = {
                                    newTagNameInput = ""
                                    showNewTagDialog = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("create_tag_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Nouveau tag", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (allTags.isEmpty()) {
                            Text(
                                text = "Aucun tag existant. Cliquez sur '+ Nouveau tag' pour en créer un.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allTags.forEach { tag ->
                                    val isSelected = selectedTagIds.contains(tag.id)
                                    TagChip(
                                        tag = tag,
                                        isSelected = isSelected,
                                        onClick = { viewModel.toggleTag(tag.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Boxes and Distribution
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
                            Column {
                                Text(
                                    text = "Rangement dans les boîtes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Répartissez les exemplaires dans vos boîtes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Box items already added
                        if (boxQuantities.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "L'objet n'est rangé dans aucune boîte pour le moment.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            val boxMap = allBoxes.associateBy { it.id }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                boxQuantities.forEach { entry ->
                                    val box = boxMap[entry.boxId]
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = box?.name ?: "Boîte #${entry.boxId}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                                if (box != null) {
                                                    Text(
                                                        text = box.code,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }

                                            // Stepper
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                FilledIconButton(
                                                    onClick = { viewModel.updateBoxQuantity(entry.boxId, entry.quantity - 1) },
                                                    shape = CircleShape,
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.surface
                                                    ),
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }

                                                Text(
                                                    text = "${entry.quantity}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 12.dp)
                                                )

                                                FilledIconButton(
                                                    onClick = { viewModel.updateBoxQuantity(entry.boxId, entry.quantity + 1) },
                                                    shape = CircleShape,
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                                    ),
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                IconButton(
                                                    onClick = { viewModel.removeBoxQuantity(entry.boxId) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Retirer",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Add box selector dropdown
                        val availableToAdd = allBoxes.filter { box ->
                            boxQuantities.none { it.boxId == box.id }
                        }

                        if (availableToAdd.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = showAddBoxDropdown,
                                onExpandedChange = { showAddBoxDropdown = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "Ajouter à une boîte...",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAddBoxDropdown) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("add_box_dropdown_trigger"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = showAddBoxDropdown,
                                    onDismissRequest = { showAddBoxDropdown = false }
                                ) {
                                    availableToAdd.forEach { box ->
                                        DropdownMenuItem(
                                            text = { Text("${box.code} — ${box.name}") },
                                            onClick = {
                                                viewModel.addBoxQuantity(box.id, 1)
                                                showAddBoxDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Photos section
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
                            Text(
                                text = "Photos",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = onNavigateToCamera,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("item_edit_take_photo_btn")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Prendre une photo")
                            }
                        }

                        if (pendingPhotos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(pendingPhotos) { path ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { viewModel.removePhoto(path) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Color.White, modifier = Modifier.size(14.dp))
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

    // New Tag Dialog
    if (showNewTagDialog) {
        val colorPalette = listOf(
            "#3B82F6", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6",
            "#EC4899", "#06B6D4", "#6366F1", "#84CC16", "#64748B"
        )
        AlertDialog(
            onDismissRequest = { showNewTagDialog = false },
            title = { Text("Nouveau tag", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTagNameInput,
                        onValueChange = { newTagNameInput = it },
                        label = { Text("Nom du tag") },
                        placeholder = { Text("Ex: urgent, voyage, outillage...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_tag_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Couleur du tag :", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colorPalette) { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedTagColorHex == hex) 3.dp else 1.dp,
                                        color = if (selectedTagColorHex == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedTagColorHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagNameInput.isNotBlank()) {
                            viewModel.createAndSelectNewTag(newTagNameInput, selectedTagColorHex)
                            showNewTagDialog = false
                        }
                    },
                    enabled = newTagNameInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_create_tag_btn")
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showNewTagDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Annuler")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
