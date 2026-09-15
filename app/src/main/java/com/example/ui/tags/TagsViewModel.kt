package com.example.ui.tags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dao.TagWithCount
import com.example.data.model.Tag
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)

    val tagsWithCount: StateFlow<List<TagWithCount>> = repository.getTagsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun saveTag(id: Long = 0L, name: String, colorHex: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Le nom du tag est obligatoire")
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.saveTag(Tag(id = id, name = trimmed, colorHex = colorHex))
                _userMessage.emit(if (id == 0L) "Tag créé" else "Tag mis à jour")
            } catch (e: Exception) {
                _userMessage.emit("Erreur : ${e.localizedMessage}")
            }
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTag(tagId)
                _userMessage.emit("Tag supprimé")
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de la suppression")
            }
        }
    }
}
