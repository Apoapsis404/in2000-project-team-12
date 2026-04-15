package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.FavoriteLocationEntity
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation

data class FavoritesScreenUiState(
    val favoritesLocationMap: Map<String, FavoriteLocation> = emptyMap()
)

class FavoritesScreenViewModel(private val dao: FavoriteDao) : ViewModel() {
    val uiState: StateFlow<FavoritesScreenUiState> = dao.getAllFavorites()
        .map { list ->
            FavoritesScreenUiState(
                favoritesLocationMap = list.associate { entity ->
                    entity.name to FavoriteLocation(
                        coordinates = LatLng(entity.latitude, entity.longitude),
                        name = entity.name,
                        address = entity.address
                    )
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesScreenUiState()
        )

    fun addFavoriteLocation(location: LatLng, name: String, address : String){
        viewModelScope.launch {
            val entity = FavoriteLocationEntity(
                name = name,
                address = address,
                latitude = location.latitude,
                longitude = location.longitude
            )
            dao.upsertFavorite(entity)
        }
    }

    fun removeFavoriteLocation(name: String){
        viewModelScope.launch {
            dao.deleteByName(name)
        }
    }

    fun changeFavoriteIcon(name: String, newIcon: ImageVector) {
        // Room databaser kan ikke lagre ImageVectors så
        // Må legge til funksjonalitet for å hente det
        // Fra strenger i stede for og så legge de strengene til
        // FavoriteLocationEntity
    }
}