package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameandAdress

@Composable
fun FavoritesScreen(favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(), navHostController: NavHostController) {

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    //string that will be added to removefavoritefunction
    var deleteplacebyname by remember { mutableStateOf<String?>(null) }
    var deleterequest by remember { mutableStateOf<Boolean>(false) }

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
    Box(modifier = Modifier){
    LazyColumn(
        modifier = Modifier
    )
        {

        items(uiState.favoritesLocationMap.toList()) { (key, favoriteLocation) ->

            var selectedbox = deleteplacebyname == favoriteLocation.name //using var because it will modify

            Box(
                modifier = Modifier.clickable{
                    deleteplacebyname =favoriteLocation.name
                }
                    .padding(6.dp)
                    .border(
                        width = if(selectedbox) 2.dp else 0.dp,
                        color = if(selectedbox) Color.Green else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
            ){
            FavoriteCard(
                title = favoriteLocation.name,
                subtitle = favoriteLocation.address,
                onButtonClick = { navHostController.navigate("graph") { popUpTo("graph") } } //seperate arrow button and name//address somehow
            )
        }
        }

    }
        Button(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                deleterequest = true
        }
        ) {
            Text("delete")
        }

    }

    if(deleterequest == true){
        AlertDialog(
            onDismissRequest = {deleterequest= false},
            title = {Text("delete?")},
            text = {Text("do you wish to delete address ${deleteplacebyname}" )},
            confirmButton = {
                TextButton(
                    onClick = {
                       deleteplacebyname?.let { name ->
                           favoritesScreenViewModel.removeFavoriteLocation(name)
                       }
                        deleterequest = false
                    }) {
                    Text("yessssssssssirrrr")
                }
            },
            dismissButton = {
                TextButton(onClick = {deleterequest = false}) {
                    Text("Nosirrrrrrrrrr")
                }
            }
        )
    }


}
