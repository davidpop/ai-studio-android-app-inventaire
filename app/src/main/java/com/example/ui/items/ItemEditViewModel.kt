package com.example.ui.items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Item
import com.example.data.model.StorageBox
import com.example.data.model.Tag
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BoxQuantityEntry(
    val boxId: Long,
    val quantity: Int
)

class ItemEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private var currentItemId: Long? = null

    var name = MutableStateFlow("")
        private set
    var description = MutableStateFlow("")
        private set
    var category = MutableStateFlow("")
        private set

    private val _selectedTagIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTagIds: StateFlow<Set<Long>> = _selectedTagIds.asStateFlow()

    private val _boxQuantities = MutableStateFlow<List<BoxQuantityEntry>>(emptyList())
    val boxQuantities: StateFlow<List<BoxQuantityEntry>> = _boxQuantities.asStateFlow()

    private val _pendingPhotos = MutableStateFlow<List<String>>(emptyList())
    val pendingPhotos: StateFlow<List<String>> = _pendingPhotos.asStateFlow()

    val allTags: StateFlow<List<Tag>> = repository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBoxes: StateFlow<List<StorageBox>> = repository.getAllBoxesWithDetails()
        .combine(MutableStateFlow(Unit)) { boxes, _ -> boxes.map { it.box } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<String>> = repository.getAllItemsWithDetails()
        .map { items ->
            val existing = items.mapNotNull { it.item.category?.trim() }.filter { it.isNotBlank() }.toSet()
            val defaults = listOf("Outillage", "Câblage", "Informatique", "Électronique", "Quincaillerie", "Matériel divers")
            (defaults + existing).distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Outillage", "Câblage", "Informatique", "Électronique", "Quincaillerie", "Matériel divers"))

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun initForEdit(itemId: Long?) {
        if (itemId == null || itemId <= 0) {
            currentItemId = null
            name.value = ""
            description.value = ""
            category.value = ""
            _selectedTagIds.value = emptySet()
            _boxQuantities.value = emptyList()
            _pendingPhotos.value = emptyList()
            return
        }

        currentItemId = itemId
        viewModelScope.launch {
            repository.getItemWithDetails(itemId).collect { itemWithDetails ->
                if (itemWithDetails != null) {
                    name.value = itemWithDetails.item.name
                    description.value = itemWithDetails.item.description ?: ""
                    category.value = itemWithDetails.item.category ?: ""
                    _selectedTagIds.value = itemWithDetails.tags.map { it.id }.toSet()
                    _boxQuantities.value = itemWithDetails.boxDistributions.map {
                        BoxQuantityEntry(it.box.id, it.quantity)
                    }
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        name.value = newName
    }

    fun onDescriptionChange(newDescription: String) {
        description.value = newDescription
    }

    fun onCategoryChange(newCategory: String) {
        category.value = newCategory
    }

    fun toggleTag(tagId: Long) {
        val current = _selectedTagIds.value.toMutableSet()
        if (current.contains(tagId)) {
            current.remove(tagId)
        } else {
            current.add(tagId)
        }
        _selectedTagIds.value = current
    }

    fun addBoxQuantity(boxId: Long, initialQty: Int = 1) {
        val current = _boxQuantities.value.toMutableList()
        val index = current.indexOfFirst { it.boxId == boxId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(BoxQuantityEntry(boxId, initialQty))
        }
        _boxQuantities.value = current
    }

    fun updateBoxQuantity(boxId: Long, newQuantity: Int) {
        val current = _boxQuantities.value.toMutableList()
        val index = current.indexOfFirst { it.boxId == boxId }
        if (index >= 0) {
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQuantity)
            }
            _boxQuantities.value = current
        }
    }

    fun removeBoxQuantity(boxId: Long) {
        val current = _boxQuantities.value.toMutableList()
        current.removeAll { it.boxId == boxId }
        _boxQuantities.value = current
    }

    fun addPhoto(photoPath: String) {
        _pendingPhotos.value = _pendingPhotos.value + photoPath
    }

    fun removePhoto(photoPath: String) {
        _pendingPhotos.value = _pendingPhotos.value - photoPath
    }

    fun createAndSelectNewTag(tagName: String, colorHex: String = "#3B82F6") {
        if (tagName.isBlank()) return
        viewModelScope.launch {
            val newTagId = repository.saveTag(Tag(name = tagName.trim(), colorHex = colorHex))
            _selectedTagIds.value = _selectedTagIds.value + newTagId
        }
    }

    fun save(onSuccess: (savedItemId: Long) -> Unit) {
        val trimmedName = name.value.trim()
        if (trimmedName.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Le nom de l'objet est obligatoire")
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val item = Item(
                    id = currentItemId ?: 0L,
                    name = trimmedName,
                    description = description.value.trim().ifBlank { null },
                    category = category.value.trim().ifBlank { null }
                )

                val boxMap = _boxQuantities.value.associate { it.boxId to it.quantity }
                val savedId = repository.saveItem(item, _selectedTagIds.value.toList(), boxMap)

                // Save pending photos
                _pendingPhotos.value.forEachIndexed { index, path ->
                    repository.addPhotoToItem(savedId, path, setAsPrimary = index == 0)
                }

                _userMessage.emit("Objet enregistré avec succès")
                onSuccess(savedId)
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de l'enregistrement : ${e.localizedMessage}")
            } finally {
                _isSaving.value = false
            }
        }
    }
}
