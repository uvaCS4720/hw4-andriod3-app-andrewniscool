package edu.nd.pmcburne.hello.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hello.data.AppDatabase
import edu.nd.pmcburne.hello.data.LocationEntity
import edu.nd.pmcburne.hello.network.NetworkModule
import edu.nd.pmcburne.hello.repository.LocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val selectedTag: String = "core",
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState

    val tags: StateFlow<List<String>>
    val filteredLocations: StateFlow<List<LocationEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LocationRepository(
            apiService = NetworkModule.providePlacemarkApi(),
            locationDao = database.locationDao()
        )

        tags = repository.getAllTags()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        filteredLocations = _uiState
            .flatMapLatest { currentState ->
                repository.getLocationsByTag(currentState.selectedTag)
            }
            .catch { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Unknown error") }
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        viewModelScope.launch {
            runCatching { repository.syncLocationsOnce() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Failed to sync data") }
                }
        }
    }

    fun onTagSelected(tag: String) {
        _uiState.update { it.copy(selectedTag = tag, errorMessage = null) }
    }
}
