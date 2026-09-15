package com.example.ui.boxes

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxEditScreen(
    boxId: Long?,
    viewModel: BoxEditViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (savedBoxId: Long) -> Unit
) {
    val context = LocalContext.current
    val code by viewModel.code.collectAsState()
    val name by viewModel.name.collectAsState()
    val label by viewModel.label.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedLocationId by viewModel.selectedLocationId.collectAsState()
    val allLocations by viewModel.allLocations.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showLocationDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(boxId) {
        viewModel.initForEdit(boxId)
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
                        text = if (boxId == null || boxId <= 0) "Nouvelle boîte" else "Modifier la boîte",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("box_edit_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save(onSaveSuccess) },
                        enabled = !isSaving && name.isNotBlank() && code.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("box_edit_save_btn")
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
                .testTag("box_edit_scroll"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Propriétés de la boîte",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Code
                        OutlinedTextField(
                            value = code,
                            onValueChange = { viewModel.onCodeChange(it) },
                            label = { Text("Code / Identifiant unique *") },
                            placeholder = { Text("Ex: BOX-0001, B01...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("box_code_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = { Text("Nom de la boîte *") },
                            placeholder = { Text("Ex: Boîte Outillage, Carton Livres...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("box_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Label
                        OutlinedTextField(
                            value = label,
                            onValueChange = { viewModel.onLabelChange(it) },
                            label = { Text("Étiquette visible / Label") },
                            placeholder = { Text("Texte noté sur l'étiquette physique") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("box_label_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { viewModel.onDescriptionChange(it) },
                            label = { Text("Description") },
                            placeholder = { Text("Notes supplémentaires, dimensions...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("box_description_input"),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Location Picker
                        Text(
                            text = "Emplacement de la boîte :",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val currentLocationName = allLocations.find { it.id == selectedLocationId }?.name
                            ?: "Aucun emplacement"

                        ExposedDropdownMenuBox(
                            expanded = showLocationDropdown,
                            onExpandedChange = { showLocationDropdown = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentLocationName,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("box_location_dropdown"),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showLocationDropdown,
                                onDismissRequest = { showLocationDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Aucun emplacement") },
                                    onClick = {
                                        viewModel.onLocationSelected(null)
                                        showLocationDropdown = false
                                    }
                                )
                                allLocations.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc.name) },
                                        onClick = {
                                            viewModel.onLocationSelected(loc.id)
                                            showLocationDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
