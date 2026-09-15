package com.example.ui.boxes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Location
import com.example.data.model.StorageBox
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoxEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private var currentBoxId: Long? = null

    var code = MutableStateFlow("")
        private set
    var name = MutableStateFlow("")
        private set
    var label = MutableStateFlow("")
        private set
    var description = MutableStateFlow("")
        private set

    private val _selectedLocationId = MutableStateFlow<Long?>(null)
    val selectedLocationId: StateFlow<Long?> = _selectedLocationId.asStateFlow()

    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri.asStateFlow()

    val allLocations: StateFlow<List<Location>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun initForEdit(boxId: Long?) {
        if (boxId == null || boxId <= 0) {
            currentBoxId = null
            name.value = ""
            label.value = ""
            description.value = ""
            _selectedLocationId.value = null
            _photoUri.value = null
            viewModelScope.launch {
                code.value = repository.generateNextBoxCode()
            }
            return
        }

        currentBoxId = boxId
        viewModelScope.launch {
            repository.getBoxWithDetails(boxId).collect { boxWithDetails ->
                if (boxWithDetails != null) {
                    code.value = boxWithDetails.box.code
                    name.value = boxWithDetails.box.name
                    label.value = boxWithDetails.box.label
                    description.value = boxWithDetails.box.description ?: ""
                    _selectedLocationId.value = boxWithDetails.box.locationId
                    _photoUri.value = boxWithDetails.box.photoUri
                }
            }
        }
    }

    fun onCodeChange(newCode: String) {
        code.value = newCode
    }

    fun onNameChange(newName: String) {
        name.value = newName
        if (label.value.isBlank()) {
            label.value = newName
        }
    }

    fun onLabelChange(newLabel: String) {
        label.value = newLabel
    }

    fun onDescriptionChange(newDescription: String) {
        description.value = newDescription
    }

    fun onLocationSelected(locationId: Long?) {
        _selectedLocationId.value = locationId
    }

    fun onPhotoSelected(uri: String?) {
        _photoUri.value = uri
    }

    fun save(onSuccess: (savedBoxId: Long) -> Unit) {
        val trimmedName = name.value.trim()
        val trimmedCode = code.value.trim()

        if (trimmedName.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Le nom de la boîte est obligatoire")
            }
            return
        }
        if (trimmedCode.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Le code de la boîte est obligatoire")
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val box = StorageBox(
                    id = currentBoxId ?: 0L,
                    code = trimmedCode,
                    name = trimmedName,
                    label = label.value.trim().ifBlank { trimmedName },
                    description = description.value.trim().ifBlank { null },
                    locationId = _selectedLocationId.value,
                    photoUri = _photoUri.value
                )

                val savedId = repository.saveBox(box)
                _userMessage.emit("Boîte enregistrée avec succès")
                onSuccess(savedId)
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de l'enregistrement : ${e.localizedMessage}")
            } finally {
                _isSaving.value = false
            }
        }
    }
}
