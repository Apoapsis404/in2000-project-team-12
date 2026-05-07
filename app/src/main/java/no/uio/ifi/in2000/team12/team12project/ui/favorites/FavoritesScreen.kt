package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel

@Composable
fun FavoritesScreen(
    favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(),
    graphScreenViewModel: GraphScreenViewModel = viewModel(),
    navHostController: NavHostController) {

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    //string that will be added to removefavoritefunction
    var deleteplacebyname by remember { mutableStateOf<String?>(null) }
    var deleterequest by remember { mutableStateOf<Boolean>(false) }
    //check if the name whants to be changed
    var changename by remember { mutableStateOf<Boolean>(false) }
    var changenameto by remember { mutableStateOf("") }
    var chosenlocation by remember { mutableStateOf<FavoriteLocation?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }



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

        //val favoriteList = uiState.favoritesLocationMap.toList()


    LazyColumn(
        modifier = Modifier
    )
        {

        items(uiState.favoritesLocationMap.toList()) { (key, favoriteLocation) ->
            //items(favoriteList) { (key, favoriteLocation) ->

            //var selectedbox = chosenlocation?.name == favoriteLocation.name
            val selectedbox = selectedName == favoriteLocation.name
            Box(
                modifier = Modifier.clickable{
                    deleteplacebyname =favoriteLocation.name
                    chosenlocation =favoriteLocation
                    selectedName = favoriteLocation.name
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
                selected = selectedbox, //
                onButtonClick = {
                    graphScreenViewModel.setMarkerPositionAndFetch(
                        favoriteLocation.coordinates
                    )
                    navHostController.navigate("graph")
                    { popUpTo("graph")
                    }
                } ,
                onButtonDelete = {
                    deleterequest = true
                },
                onButtonChangName = {
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
            title = {Text("Delete location")},
            text = {Text("Do you wish to delete your location ${deleteplacebyname}?" )},
            confirmButton = {
                TextButton(
                    onClick = {
                       deleteplacebyname?.let { name ->
                           favoritesScreenViewModel.removeFavoriteLocation(name)
                       }
                        deleterequest = false
                        chosenlocation =null
                    }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {deleterequest = false}) {
                    Text("Cancel")
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
