package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.FavoriteCard

@Composable
fun FavoritesScreen(favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(), navHostController: NavHostController){

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    /*
    LaunchedEffect(uiState.favoritesLocationList) {

        //dersom det ikke er noen favoritt, legges til default hardkodet plasser
        if (uiState.favoritesLocationList.isEmpty()) {
            favoritesScreenViewModel.addFavoriteLocation(
                location = LatLng(1.1, 1.1),
                "Pappas gård",
                "Fadervegen 1"
            )
            favoritesScreenViewModel.addFavoriteLocation(
                location = LatLng(1.1, 1.1),
                "Bakgården",
                "Bak huset"
            )
            favoritesScreenViewModel.addFavoriteLocation(
                location = LatLng(1.1, 1.1),
                "Skjærgården",
                "Husmans gate 10"
            )
            favoritesScreenViewModel.addFavoriteLocation(
                location = LatLng(1.1, 1.1),
                "Forigården",
                "Fortiden"
            )
            favoritesScreenViewModel.addFavoriteLocation(
                location = LatLng(2.2, 2.2),
                name = "Stellan Skaarsgården",
                address = "Sverige"
            )

        }
    */
        /*
        //dersom listenseg vil noe legges til som favoritt
        if (uiState.favoritesLocationList.isEmpty() == false){
            uiState.favoritesLocationList.forEach{ favorite ->
                favoritesScreenViewModel.addFavoriteLocation(
                    location = favorite.coordinates,
                    name = favorite.name,
                    address = favorite.address
                )

            }
        }*/
   // }
        /*
        //dersom det endrer seg. noe vil legges til som favoritt
        if (uiState.favoritesLocationList.isEmpty() == false){
            uiState.favoritesLocationList.forEach{ favorite ->
                favoritesScreenViewModel.addFavoriteLocation(
                    location = favorite.coordinates,
                    name = favorite.name,
                    address = favorite.address
                )

            }
        }*/

    LazyColumn(modifier = Modifier ) {
        items(uiState.favoritesLocationList) { favoriteLocation ->
            FavoriteCard(
                title = favoriteLocation.name,
                subtitle = favoriteLocation.address,
                onButtonClick = {navHostController.navigate("graph"){popUpTo("graph")} }
            )
        }
    }
}