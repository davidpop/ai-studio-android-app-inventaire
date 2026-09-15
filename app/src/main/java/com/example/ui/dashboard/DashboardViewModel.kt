package com.example.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BoxWithDetails
import com.example.data.model.DashboardStats
import com.example.data.model.ItemWithDetails
import com.example.data.repository.InventoryRepository
import com.example.util.BackupManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getInstance(application)
    private val database = AppDatabase.getInstance(application)
    private val backupManager = BackupManager(application, database)

    val stats: StateFlow<DashboardStats> = repository.getDashboardStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val recentItems: StateFlow<List<ItemWithDetails>> = repository.getAllItemsWithDetails()
        .map { items -> items.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBoxes: StateFlow<List<BoxWithDetails>> = repository.getAllBoxesWithDetails()
        .map { boxes -> boxes.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkSeedDemoData()
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                repository.resetDemoData()
                _userMessage.emit("Données de démonstration réinitialisées avec succès")
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de la réinitialisation : ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun exportBackup(onExportReady: (jsonString: String) -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val json = backupManager.exportToJson()
                onExportReady(json)
                _userMessage.emit("Sauvegarde générée avec succès")
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de l'exportation : ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val success = backupManager.importFromJson(jsonString)
                if (success) {
                    _userMessage.emit("Sauvegarde restaurée avec succès")
                } else {
                    _userMessage.emit("Format de fichier JSON invalide")
                }
            } catch (e: Exception) {
                _userMessage.emit("Erreur lors de la restauration : ${e.localizedMessage}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
