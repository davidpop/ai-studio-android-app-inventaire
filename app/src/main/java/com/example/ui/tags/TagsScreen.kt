package com.example.ui.tags

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.TagWithCount
import com.example.data.model.Tag
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    viewModel: TagsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tagsWithCount by viewModel.tagsWithCount.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var tagNameInput by remember { mutableStateOf("") }
    var tagColorHexInput by remember { mutableStateOf("#3B82F6") }

    var deletingTag by remember { mutableStateOf<Tag?>(null) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val palette = listOf(
        "#3B82F6", "#EF4444", "#10B981", "#F59E0B", "#8B5CF6",
        "#EC4899", "#06B6D4", "#6366F1", "#84CC16", "#64748B"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tags & Catégories (${tagsWithCount.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("tags_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTag = null
                    tagNameInput = ""
                    tagColorHexInput = "#3B82F6"
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_tag_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un tag")
            }
        }
    ) { padding ->
        if (tagsWithCount.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.LocalOffer,
                title = "Aucun tag",
                subtitle = "Les tags permettent de catégoriser librement vos objets et de les retrouver facilement.",
                actionButtonText = "+ Créer mon premier tag",
                onActionClick = {
                    editingTag = null
                    tagNameInput = ""
                    tagColorHexInput = "#3B82F6"
                    showEditDialog = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                testTag = "tags_empty_state"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("tags_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tagsWithCount, key = { it.id }) { item ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(item.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tag_item_${item.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${item.itemCount} objet(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    editingTag = Tag(id = item.id, name = item.name, colorHex = item.colorHex)
                                    tagNameInput = item.name
                                    tagColorHexInput = item.colorHex
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    deletingTag = Tag(id = item.id, name = item.name, colorHex = item.colorHex)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
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

    // Add / Edit Tag Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingTag == null) "Nouveau tag" else "Modifier le tag",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = tagNameInput,
                        onValueChange = { tagNameInput = it },
                        label = { Text("Nom du tag *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tag_name_dialog_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Couleur associée :", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(palette) { hex ->
                            val c = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (tagColorHexInput == hex) 3.dp else 1.dp,
                                        color = if (tagColorHexInput == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { tagColorHexInput = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveTag(
                            id = editingTag?.id ?: 0L,
                            name = tagNameInput,
                            colorHex = tagColorHexInput
                        )
                        showEditDialog = false
                    },
                    enabled = tagNameInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
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
    deletingTag?.let { tag ->
        ConfirmDeleteDialog(
            title = "Supprimer le tag",
            message = "Voulez-vous supprimer le tag '${tag.name}' ? Il sera retiré des objets associés sans supprimer les objets eux-mêmes.",
            onConfirm = {
                viewModel.deleteTag(tag.id)
                deletingTag = null
            },
            onDismiss = { deletingTag = null }
        )
    }
}
