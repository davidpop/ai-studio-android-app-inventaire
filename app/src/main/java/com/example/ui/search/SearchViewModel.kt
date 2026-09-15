package com.example.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.InventoryRepository
import com.example.data.repository.SearchResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults: StateFlow<SearchResults> = _query.flatMapLatest { q ->
        repository.search(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }
}
