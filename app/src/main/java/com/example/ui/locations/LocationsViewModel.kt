package com.example.ui.locations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Location
import com.example.data.model.LocationNode
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

class LocationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    val locationTree: StateFlow<List<LocationNode>> = repository.getLocationTree()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocations: StateFlow<List<Location>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun saveLocation(id: Long = 0L, name: String, parentId: Long?, description: String?) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Le nom de l'emplacement est obligatoire")
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.saveLocation(
                    Location(
                        id = id,
                        name = trimmedName,
                        parentId = parentId,
                        description = description?.trim()?.ifBlank { null }
                    )
                )
                _userMessage.emit(if (id == 0L) "Emplacement créé" else "Emplacement mis à jour")
            } catch (e: Exception) {
                _userMessage.emit("Erreur : ${e.localizedMessage}")
            }
        }
    }

    fun moveLocation(locationId: Long, newParentId: Long?) {
        viewModelScope.launch {
            try {
                repository.moveLocation(locationId, newParentId)
                _userMessage.emit("Emplacement déplacé")
            } catch (e: Exception) {
                _userMessage.emit("Erreur : ${e.localizedMessage}")
            }
        }
    }

    fun deleteLocation(locationId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteLocation(locationId)
                _userMessage.emit("Emplacement supprimé")
            } catch (e: Exception) {
                _userMessage.emit("Erreur : ${e.localizedMessage}")
            }
        }
    }
}
