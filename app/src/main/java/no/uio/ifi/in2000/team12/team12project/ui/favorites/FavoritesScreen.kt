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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameandAdress

@Composable
fun FavoritesScreen(favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(), navHostController: NavHostController) {

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    //string that will be added to removefavoritefunction
    var deleteplacebyname by remember { mutableStateOf<String?>(null) }
    var deleterequest by remember { mutableStateOf<Boolean>(false) }
    //check if the name whants to be changed
    var changename by remember { mutableStateOf<Boolean>(false) }
    var changenameto by remember { mutableStateOf("") }
    var changenameof by remember { mutableStateOf<FavoriteLocation?>(null) }


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
    //check for newly added to favorites
    LaunchedEffect(favoritesScreenViewModel.lastfavoriteadded,uiState.favoritesLocationMap) {
        val newname = favoritesScreenViewModel.lastfavoriteadded
        if(newname != null){
            val item = uiState.favoritesLocationMap.values.find{it.name == newname}//uiState.favoritesLocationMap[newname]//[deleteplacebyname]//[newname]
            if(item == null) return@LaunchedEffect
            //if(item != null){
                changenameof = item
                deleteplacebyname = item.name
                changename = true
            //}
            favoritesScreenViewModel.lastfavoriteadded = null
        }
    }

    Box(modifier = Modifier){
    LazyColumn(
        modifier = Modifier
    )
        {

        items(uiState.favoritesLocationMap.toList()) { (key, favoriteLocation) ->

            //var selectedbox = deleteplacebyname == favoriteLocation.name //using var because it will modify
            var selectedbox = changenameof?.name == favoriteLocation.name
            Box(
                modifier = Modifier.clickable{
                    deleteplacebyname =favoriteLocation.name
                    changenameof =favoriteLocation
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
                if(changenameof != null) {
                    deleterequest = true
                }
        }
        ) {
            Text("delete")
        }

        Button(
            modifier = Modifier.align(Alignment.BottomStart),
            onClick = {
                if(changenameof != null) {
                    changename = true
                }
            }
        ) {
            Text("add/change name")
        }

    }

    if(deleterequest == true && changenameof !=null){
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
                        changenameof =null
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

    if (changename && changenameof !=null) {

            AlertDialog(
            onDismissRequest = {
                changename = false
                changenameto = ""

            },
            title = { Text("Enter new name") },
            text = {
                TextField(
                    value = changenameto,
                    onValueChange = { changenameto = it },
                    placeholder = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    //changenameof?.let {
                        favoritesScreenViewModel.changeFavoriteLocationName(
                            prevname = changenameof!!.name,//it.name,
                            changenameto = changenameto
                        )
                    //}
                    changenameto = ""
                    changename = false

                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    changename = false

                }) {
                    Text("Cancel")
                }
            }
        )
    }


}
