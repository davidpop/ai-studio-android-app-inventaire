package com.example.ui.boxes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BoxWithDetails
import com.example.data.model.Location
import com.example.data.model.StorageBox
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoxesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLocationId = MutableStateFlow<Long?>(null)
    val selectedLocationId: StateFlow<Long?> = _selectedLocationId.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val allLocations: StateFlow<List<Location>> = repository.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBoxesWithDetails: StateFlow<List<BoxWithDetails>> = repository.getAllBoxesWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredBoxes: StateFlow<List<BoxWithDetails>> = combine(
        allBoxesWithDetails,
        _searchQuery,
        _selectedLocationId
    ) { boxes, query, locId ->
        var result = boxes
        if (query.isNotBlank()) {
            result = result.filter { b ->
                StringUtils.matchesFuzzy(b.box.name, query) ||
                        StringUtils.matchesFuzzy(b.box.code, query) ||
                        StringUtils.matchesFuzzy(b.box.label, query) ||
                        StringUtils.matchesFuzzy(b.box.description, query) ||
                        StringUtils.matchesFuzzy(b.locationPath, query) ||
                        b.items.any { StringUtils.matchesFuzzy(it.item.name, query) }
            }
        }
        if (locId != null) {
            result = result.filter { it.box.locationId == locId }
        }
        result.sortedBy { it.box.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedLocationId(locationId: Long?) {
        _selectedLocationId.value = if (_selectedLocationId.value == locationId) null else locationId
    }

    fun deleteBox(boxId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteBox(boxId)
            _userMessage.emit("Boîte supprimée")
            onDeleted()
        }
    }

    fun reassignAndDeleteBox(fromBoxId: Long, toBoxId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.reassignBoxAndThenDelete(fromBoxId, toBoxId)
            _userMessage.emit("Objets transférés et boîte supprimée")
            onDeleted()
        }
    }
}
