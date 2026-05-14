package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.FavoriteLocationEntity
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation

data class FavoritesScreenUiState(
    val favoritesLocationMap: Map<String, FavoriteLocation> = emptyMap(),
    val selectedLocation: FavoriteLocation? = null,
    val showDeleteDialog: Boolean = false,
    val showRenameDialog: Boolean = false,
    val renameTextFieldValue: String = "",
    val showDuplicateError: Boolean = false
)

class FavoritesScreenViewModel(private val dao: FavoriteDao) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesScreenUiState())



    val uiState: StateFlow<FavoritesScreenUiState> = dao.getAllFavorites()
        .combine(_uiState) { list, interaction ->
            // Map the DB list to our FavoriteLocation objects
            val locationMap = list.associate { entity ->
                entity.name to FavoriteLocation(
                    coordinates = LatLng(entity.latitude, entity.longitude),
                    name = entity.name,
                    address = entity.address
                )
            }
            // Merge DB data into the interaction state
            interaction.copy(favoritesLocationMap = locationMap)

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesScreenUiState()
        )

    // Events flow UP from UI
    fun onLocationSelected(location: FavoriteLocation?) {
        _uiState.update { it.copy(selectedLocation = location) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun setShowRenameDialog(show: Boolean, initialText: String = "") {
        _uiState.update {
            it.copy(showRenameDialog = show, renameTextFieldValue = initialText)
        }
    }

    fun onRenameTextChanged(newText: String) {
        _uiState.update { it.copy(renameTextFieldValue = newText) }
    }

    fun dismissDuplicateError() {
        _uiState.update { it.copy(showDuplicateError = false) }
    }

    fun addFavoriteLocation(location: LatLng, name: String, address : String){
        viewModelScope.launch {
            // Check for duplicate in the current favorites list
            val isDuplicate = uiState.value.favoritesLocationMap.values.any {
                it.coordinates.latitude == location.latitude && it.coordinates.longitude == location.longitude
            } || uiState.value.favoritesLocationMap.containsKey(name)

            if (isDuplicate) {
                _uiState.update { it.copy(showDuplicateError = true) }
                return@launch
            }

            val entity = FavoriteLocationEntity(
                name = name,
                address = address,
                latitude = location.latitude,
                longitude = location.longitude
            )
            dao.upsertFavorite(entity)

            val addedLoc = FavoriteLocation(location, name, address)
            _uiState.update {
                it.copy(
                    selectedLocation = addedLoc,
                    showRenameDialog = true,
                    renameTextFieldValue = "" // Ready for user input
                )
            }
        }
    }
    fun confirmDelete() {
        val name = _uiState.value.selectedLocation?.name ?: return
        viewModelScope.launch {
            dao.deleteByName(name)
            _uiState.update { it.copy(showDeleteDialog = false, selectedLocation = null) }
        }
    }

    fun confirmRename() {
        val oldName = _uiState.value.selectedLocation?.name ?: return
        val newName = _uiState.value.renameTextFieldValue
        if (newName.isBlank()) return

        // Check if name already exists (and it's not the same location we are currently renaming)
        if (uiState.value.favoritesLocationMap.containsKey(newName) && newName != oldName) {
            _uiState.update { it.copy(showDuplicateError = true) }
            return
        }

        viewModelScope.launch {
            dao.changeFavoriteLocationName(oldName, newName)
            _uiState.update { it.copy(showRenameDialog = false, selectedLocation = null) }
        }
    }
}