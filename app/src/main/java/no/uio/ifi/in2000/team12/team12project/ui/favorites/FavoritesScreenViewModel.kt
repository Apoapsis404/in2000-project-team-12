package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    val renameTextFieldValue: String = ""
)

class FavoritesScreenViewModel(private val dao: FavoriteDao) : ViewModel() {
    //var lastfavoriteadded by mutableStateOf<String?>(null) //so it know the newly added favorite to delete
    //private val _lastfavoriteadded = MutableSharedFlow<String?>()
    //val lastfavoriteadded = _lastfavoriteadded.asSharedFlow()
    private val _interactionState = MutableStateFlow(FavoritesScreenUiState())



    val uiState: StateFlow<FavoritesScreenUiState> = dao.getAllFavorites()
        .combine(_interactionState) { list, interaction ->
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
        /*.map { list ->
            FavoritesScreenUiState(
                favoritesLocationMap = list.associate { entity ->
                    entity.name to FavoriteLocation(
                        coordinates = LatLng(entity.latitude, entity.longitude),
                        name = entity.name,
                        address = entity.address
                    )
                }
            )
        }*/.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesScreenUiState()
        )
    // Events flow UP from UI
    fun onLocationSelected(location: FavoriteLocation?) {
        _interactionState.update { it.copy(selectedLocation = location) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _interactionState.update { it.copy(showDeleteDialog = show) }
    }

    fun setShowRenameDialog(show: Boolean, initialText: String = "") {
        _interactionState.update {
            it.copy(showRenameDialog = show, renameTextFieldValue = initialText)
        }
    }

    fun onRenameTextChanged(newText: String) {
        _interactionState.update { it.copy(renameTextFieldValue = newText) }
    }
    fun addFavoriteLocation(location: LatLng, name: String, address : String){
        viewModelScope.launch {
            val entity = FavoriteLocationEntity(
                name = name,
                address = address,
                latitude = location.latitude,
                longitude = location.longitude
            )
            dao.upsertFavorite(entity)

            //lastfavoriteadded = name
            //_lastfavoriteadded.emit(name)
            // Replaces the LaunchedEffect: Immediately prepare the UI for renaming
            val addedLoc = FavoriteLocation(location, name, address)
            _interactionState.update {
                it.copy(
                    selectedLocation = addedLoc,
                    showRenameDialog = true,
                    renameTextFieldValue = "" // Ready for user input
                )
            }
        }
    }
    fun confirmDelete() {
        val name = _interactionState.value.selectedLocation?.name ?: return
        viewModelScope.launch {
            dao.deleteByName(name)
            _interactionState.update { it.copy(showDeleteDialog = false, selectedLocation = null) }
        }
    }

    fun confirmRename() {
        val oldName = _interactionState.value.selectedLocation?.name ?: return
        val newName = _interactionState.value.renameTextFieldValue
        if (newName.isBlank()) return

        viewModelScope.launch {
            dao.changeFavoriteLocationName(oldName, newName)
            _interactionState.update { it.copy(showRenameDialog = false, selectedLocation = null) }
        }
    }
    /*
    fun removeFavoriteLocation(name: String){
        viewModelScope.launch {
            dao.deleteByName(name)
        }
    }

//    fun changeFavoriteIcon(name: String, newIcon: ImageVector) {
//        // Room databaser kan ikke lagre ImageVectors så
//        // Må legge til funksjonalitet for å hente det
//        // Fra strenger i stede for og så legge de strengene til
//        // FavoriteLocationEntity
//    }
    fun changeFavoriteLocationName(prevname: String, changenameto: String){
        viewModelScope.launch {
            dao.changeFavoriteLocationName(prevname,changenameto)
        }
    }*/

}