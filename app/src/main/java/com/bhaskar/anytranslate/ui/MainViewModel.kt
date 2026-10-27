package com.bhaskar.anytranslate.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.anytranslate.AnyTranslateApp
import com.bhaskar.anytranslate.data.api.OnlineTranslationService
import com.bhaskar.anytranslate.data.local.TranslationHistoryEntity
import com.bhaskar.anytranslate.data.ml.MLKitTranslationManager
import com.bhaskar.anytranslate.data.ml.ModelDownloadState
import com.bhaskar.anytranslate.data.model.CombinedTranslation
import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SelectedTargetFilter {
    ALL,
    ENGLISH,
    GERMAN,
    NEPALI,
    HINDI
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AnyTranslateApp
    private val mlKitManager = MLKitTranslationManager(app)
    private val onlineService = OnlineTranslationService()
    private val repository = TranslationRepository(
        dictionaryDao = app.database.dictionaryDao(),
        historyDao = app.database.historyDao(),
        mlKitManager = mlKitManager,
        onlineService = onlineService
    )

    val modelDownloadState: StateFlow<ModelDownloadState> = mlKitManager.downloadState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Null sourceLanguage means Auto-Detect
    private val _sourceLanguage = MutableStateFlow<Language?>(null)
    val sourceLanguage: StateFlow<Language?> = _sourceLanguage.asStateFlow()

    private val _selectedTargetFilter = MutableStateFlow(SelectedTargetFilter.ALL)
    val selectedTargetFilter: StateFlow<SelectedTargetFilter> = _selectedTargetFilter.asStateFlow()

    private val _currentTranslation = MutableStateFlow<CombinedTranslation?>(null)
    val currentTranslation: StateFlow<CombinedTranslation?> = _currentTranslation.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    val historyList: StateFlow<List<TranslationHistoryEntity>> = repository.historyFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoritesList: StateFlow<List<TranslationHistoryEntity>> = repository.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            mlKitManager.prepareModels()
        }
    }

    fun setSourceLanguage(language: Language?) {
        _sourceLanguage.value = language
    }

    fun setTargetFilter(filter: SelectedTargetFilter) {
        _selectedTargetFilter.value = filter
    }

    fun translate(query: String) {
        if (query.isBlank()) {
            viewModelScope.launch {
                _errorMessage.emit("Please enter text to translate")
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.translate(query, _sourceLanguage.value)
                _currentTranslation.value = result
            } catch (e: Exception) {
                _errorMessage.emit(e.localizedMessage ?: "Translation failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(query: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            val newStatus = !isCurrentlyFavorite
            repository.toggleFavorite(query, newStatus)

            val current = _currentTranslation.value
            if (current != null && current.query.equals(query, ignoreCase = true)) {
                _currentTranslation.value = current.copy(
                    translations = current.translations.map { it.copy(isBookmarked = newStatus) }
                )
            }
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mlKitManager.close()
    }
}
