package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation

data class FavoritesScreenUiState(
    val favoritesLocationList: List<FavoriteLocation> = emptyList()
)

class FavoritesScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesScreenUiState())
    val uiState = _uiState.asStateFlow()

    fun addFavoriteLocation(location: LatLng, name: String, address : String){
        val favoriteLocation = FavoriteLocation(location, name, address)
        val tempList = _uiState.value.favoritesLocationList + favoriteLocation

        _uiState.update { it.copy(favoritesLocationList = tempList) }
    }
}