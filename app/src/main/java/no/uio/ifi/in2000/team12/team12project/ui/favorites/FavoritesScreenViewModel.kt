package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation

data class FavoritesScreenUiState(
    val favoritesLocationMap: Map<String, FavoriteLocation> = emptyMap()
)

class FavoritesScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun addFavoriteLocation(location: LatLng, name: String, address : String){
        if (_uiState.value.favoritesLocationMap.containsKey(name)) {
            throw IllegalArgumentException("Favorite location with name $name already exists")
        }
        val favoriteLocation = FavoriteLocation(location, name, address)

        _uiState.update { it.copy(
            favoritesLocationMap = it.favoritesLocationMap + (favoriteLocation.name to favoriteLocation)
        ) }
    }

    fun removeFavoriteLocation(name: String){
        if (_uiState.value.favoritesLocationMap.containsKey(name)){
            _uiState.update { it.copy(
                favoritesLocationMap = it.favoritesLocationMap - name
            ) }
        } else {
            throw IllegalArgumentException("Favorite location with name $name not found")
        }
    }

    fun changeFavoriteIcon(name: String, newIcon: ImageVector){
        if (_uiState.value.favoritesLocationMap.containsKey(name)){
            _uiState.update { it ->
                val newMap = it.favoritesLocationMap.toMutableMap().apply {
                    this[name] = this[name]?.copy(icon = newIcon) ?: return@apply
                }
                it.copy(favoritesLocationMap = newMap)
            }
        } else {
            throw IllegalArgumentException("Favorite location with name $name not found")
        }
    }
}