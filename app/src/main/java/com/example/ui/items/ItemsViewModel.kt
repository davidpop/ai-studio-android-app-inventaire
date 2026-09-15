package com.example.ui.items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ItemWithDetails
import com.example.data.model.StorageBox
import com.example.data.model.Tag
import com.example.data.repository.InventoryRepository
import com.example.util.StringUtils
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

enum class ItemSortOption(val displayName: String) {
    NAME_ASC("Nom (A-Z)"),
    NAME_DESC("Nom (Z-A)"),
    QUANTITY_DESC("Quantité (décroissant)"),
    QUANTITY_ASC("Quantité (croissant)"),
    RECENT("Modifiés récemment")
}

private data class ItemFilterState(
    val query: String,
    val tagId: Long?,
    val category: String?,
    val boxId: Long?,
    val sort: ItemSortOption
)

class ItemsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTagId = MutableStateFlow<Long?>(null)
    val selectedTagId: StateFlow<Long?> = _selectedTagId.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedBoxId = MutableStateFlow<Long?>(null)
    val selectedBoxId: StateFlow<Long?> = _selectedBoxId.asStateFlow()

    private val _sortOption = MutableStateFlow(ItemSortOption.NAME_ASC)
    val sortOption: StateFlow<ItemSortOption> = _sortOption.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val allTags: StateFlow<List<Tag>> = repository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBoxes: StateFlow<List<StorageBox>> = repository.getAllBoxesWithDetails()
        .map { boxes -> boxes.map { it.box } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allItems: StateFlow<List<ItemWithDetails>> = repository.getAllItemsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val filterStateFlow = combine(
        _searchQuery,
        _selectedTagId,
        _selectedCategory,
        _selectedBoxId,
        _sortOption
    ) { query, tagId, category, boxId, sort ->
        ItemFilterState(query, tagId, category, boxId, sort)
    }

    val filteredItems: StateFlow<List<ItemWithDetails>> = combine(
        allItems,
        filterStateFlow
    ) { items, filters ->
        var result = items

        // Search filter
        if (filters.query.isNotBlank()) {
            result = result.filter { itm ->
                StringUtils.matchesFuzzy(itm.item.name, filters.query) ||
                        StringUtils.matchesFuzzy(itm.item.description, filters.query) ||
                        StringUtils.matchesFuzzy(itm.item.category, filters.query) ||
                        itm.tags.any { StringUtils.matchesFuzzy(it.name, filters.query) } ||
                        itm.boxDistributions.any {
                            StringUtils.matchesFuzzy(it.box.name, filters.query) ||
                                    StringUtils.matchesFuzzy(it.box.code, filters.query)
                        }
            }
        }

        // Tag filter
        if (filters.tagId != null) {
            result = result.filter { itm -> itm.tags.any { it.id == filters.tagId } }
        }

        // Category filter
        if (filters.category != null) {
            result = result.filter { itm ->
                itm.item.category?.equals(filters.category, ignoreCase = true) == true
            }
        }

        // Box filter
        if (filters.boxId != null) {
            result = result.filter { itm -> itm.boxDistributions.any { it.box.id == filters.boxId } }
        }

        // Sort
        when (filters.sort) {
            ItemSortOption.NAME_ASC -> result.sortedBy { it.item.name.lowercase() }
            ItemSortOption.NAME_DESC -> result.sortedByDescending { it.item.name.lowercase() }
            ItemSortOption.QUANTITY_DESC -> result.sortedByDescending { it.totalQuantity }
            ItemSortOption.QUANTITY_ASC -> result.sortedBy { it.totalQuantity }
            ItemSortOption.RECENT -> result.sortedByDescending { it.item.updatedAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = allItems.map { items ->
        items.mapNotNull { it.item.category?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTagId(tagId: Long?) {
        _selectedTagId.value = if (_selectedTagId.value == tagId) null else tagId
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun setSelectedBoxId(boxId: Long?) {
        _selectedBoxId.value = if (_selectedBoxId.value == boxId) null else boxId
    }

    fun setSortOption(option: ItemSortOption) {
        _sortOption.value = option
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedTagId.value = null
        _selectedCategory.value = null
        _selectedBoxId.value = null
    }

    fun moveQuantity(itemId: Long, fromBoxId: Long, toBoxId: Long, quantity: Int) {
        viewModelScope.launch {
            val success = repository.moveQuantity(itemId, fromBoxId, toBoxId, quantity)
            if (success) {
                _userMessage.emit("Déplacement de $quantity exemplaire(s) effectué")
            } else {
                _userMessage.emit("Quantité insuffisante ou boîte invalide")
            }
        }
    }

    fun adjustQuantity(itemId: Long, boxId: Long, newQuantity: Int, currentQuantity: Int) {
        viewModelScope.launch {
            val delta = newQuantity - currentQuantity
            repository.adjustQuantityInBox(itemId, boxId, delta)
            _userMessage.emit("Quantité mise à jour : $newQuantity")
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
            _userMessage.emit("Objet supprimé de l'inventaire")
        }
    }
}
