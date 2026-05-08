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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameandAdress
import no.uio.ifi.in2000.team12.team12project.ui.theme.sec2

@Composable
fun FavoritesScreen(favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(), navHostController: NavHostController) {

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    //string that will be added to removefavoritefunction
    var deleteplacebyname by remember { mutableStateOf<String?>(null) }
    var deleterequest by remember { mutableStateOf<Boolean>(false) }
    //check if the name whants to be changed
    var changename by remember { mutableStateOf<Boolean>(false) }
    var changenameto by remember { mutableStateOf("") }
    var chosenlocation by remember { mutableStateOf<FavoriteLocation?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }

    //check for newly added to favorites
    LaunchedEffect(favoritesScreenViewModel.lastfavoriteadded,uiState.favoritesLocationMap) {
        val newname = favoritesScreenViewModel.lastfavoriteadded
        if(newname != null){
            val item = uiState.favoritesLocationMap.values.find{it.name == newname}//uiState.favoritesLocationMap[newname]//[deleteplacebyname]//[newname]
            if(item == null) return@LaunchedEffect
            //if(item != null){
                chosenlocation = item
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

            //var selectedbox = chosenlocation?.name == favoriteLocation.name
            val selectedbox = selectedName == favoriteLocation.name
            Box(
                modifier = Modifier.clickable{
                    deleteplacebyname =favoriteLocation.name
                    chosenlocation =favoriteLocation
                    selectedName = favoriteLocation.name
                }
                    .padding(6.dp)

            ){
            FavoriteCard(
                title = favoriteLocation.name,
                subtitle = favoriteLocation.address,
                selected = selectedbox, //
                onGraphButtonClick = {
                    navHostController.navigate("graph")
                    { popUpTo("graph")
                    }
                } , //
                onDeleteButtonClick = {
                    deleterequest = true
                },
                onChangeNameButtonClick = {
                    changename = true
                }
            )
        }
        }

    }
    }

    if(deleterequest && chosenlocation !=null){
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
                        chosenlocation =null
                    }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {deleterequest = false}) {
                    Text("No")
                }
            }
        )
    }

    if (changename && chosenlocation != null) {

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
                            prevname = chosenlocation!!.name,//it.name,
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
