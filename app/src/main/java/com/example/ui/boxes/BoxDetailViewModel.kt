package com.example.ui.boxes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BoxWithDetails
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoxDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private val _boxId = MutableStateFlow<Long?>(null)

    val boxWithDetails: StateFlow<BoxWithDetails?> = _boxId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getBoxWithDetails(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBoxes: StateFlow<List<StorageBox>> = repository.getAllBoxesWithDetails()
        .combine(MutableStateFlow(Unit)) { boxes, _ -> boxes.map { it.box } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocations: StateFlow<List<Location>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun setBoxId(id: Long) {
        _boxId.value = id
    }

    fun updateLocation(newLocationId: Long?) {
        val current = boxWithDetails.value?.box ?: return
        viewModelScope.launch {
            repository.saveBox(current.copy(locationId = newLocationId))
            _userMessage.emit("Emplacement mis à jour")
        }
    }

    fun setPhotoUri(photoUri: String) {
        val current = boxWithDetails.value?.box ?: return
        viewModelScope.launch {
            repository.saveBox(current.copy(photoUri = photoUri))
            _userMessage.emit("Photo de la boîte mise à jour")
        }
    }

    fun moveItemQuantity(itemId: Long, toBoxId: Long, quantity: Int) {
        val boxId = _boxId.value ?: return
        viewModelScope.launch {
            val success = repository.moveQuantity(itemId, boxId, toBoxId, quantity)
            if (success) {
                _userMessage.emit("$quantity exemplaire(s) déplacé(s)")
            } else {
                _userMessage.emit("Erreur lors du déplacement")
            }
        }
    }

    fun adjustItemQuantity(itemId: Long, newQuantity: Int, currentQuantity: Int) {
        val boxId = _boxId.value ?: return
        viewModelScope.launch {
            val delta = newQuantity - currentQuantity
            repository.adjustQuantityInBox(itemId, boxId, delta)
            _userMessage.emit("Quantité mise à jour : $newQuantity")
        }
    }

    fun deleteBox(onDeleted: () -> Unit) {
        val id = _boxId.value ?: return
        viewModelScope.launch {
            repository.deleteBox(id)
            _userMessage.emit("Boîte supprimée")
            onDeleted()
        }
    }

    fun reassignAndDeleteBox(toBoxId: Long, onDeleted: () -> Unit) {
        val id = _boxId.value ?: return
        viewModelScope.launch {
            repository.reassignBoxAndThenDelete(id, toBoxId)
            _userMessage.emit("Contenu transféré et boîte supprimée")
            onDeleted()
        }
    }
}
