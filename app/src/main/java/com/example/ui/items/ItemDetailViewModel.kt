package com.example.ui.items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ItemPhoto
import com.example.data.model.ItemWithDetails
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

class ItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private val _itemId = MutableStateFlow<Long?>(null)

    val itemWithDetails: StateFlow<ItemWithDetails?> = _itemId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getItemWithDetails(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBoxes: StateFlow<List<StorageBox>> = repository.getAllBoxesWithDetails()
        .combine(MutableStateFlow(Unit)) { boxes, _ -> boxes.map { it.box } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _selectedPhotoForViewer = MutableStateFlow<ItemPhoto?>(null)
    val selectedPhotoForViewer: StateFlow<ItemPhoto?> = _selectedPhotoForViewer.asStateFlow()

    fun setItemId(id: Long) {
        _itemId.value = id
    }

    fun openPhotoViewer(photo: ItemPhoto) {
        _selectedPhotoForViewer.value = photo
    }

    fun closePhotoViewer() {
        _selectedPhotoForViewer.value = null
    }

    fun addPhoto(photoPath: String) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            repository.addPhotoToItem(id, photoPath)
            _userMessage.emit("Photo ajoutée")
        }
    }

    fun setPrimaryPhoto(photo: ItemPhoto) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            repository.setPrimaryPhoto(id, photo.id, photo.photoPath)
            _userMessage.emit("Photo principale définie")
            closePhotoViewer()
        }
    }

    fun deletePhoto(photo: ItemPhoto) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            repository.deletePhoto(id, photo.id, photo.photoPath)
            _userMessage.emit("Photo supprimée")
            closePhotoViewer()
        }
    }

    fun moveQuantity(fromBoxId: Long, toBoxId: Long, quantity: Int) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            val success = repository.moveQuantity(id, fromBoxId, toBoxId, quantity)
            if (success) {
                _userMessage.emit("$quantity exemplaire(s) déplacé(s)")
            } else {
                _userMessage.emit("Erreur lors du déplacement")
            }
        }
    }

    fun adjustQuantity(boxId: Long, newQuantity: Int, currentQuantity: Int) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            val delta = newQuantity - currentQuantity
            repository.adjustQuantityInBox(id, boxId, delta)
            _userMessage.emit("Quantité mise à jour : $newQuantity")
        }
    }

    fun deleteItem(onDeleted: () -> Unit) {
        val id = _itemId.value ?: return
        viewModelScope.launch {
            repository.deleteItem(id)
            _userMessage.emit("Objet supprimé")
            onDeleted()
        }
    }
}
