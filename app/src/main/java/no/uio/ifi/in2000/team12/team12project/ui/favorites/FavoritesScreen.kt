package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel

@Composable
fun FavoritesScreen(
    favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(),
    graphScreenViewModel: GraphScreenViewModel = viewModel(),
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    navHostController: NavHostController) {

    val uiState by favoritesScreenViewModel.uiState.collectAsState()
    //string that will be added to removefavoritefunction
    var deletePlaceByName by remember { mutableStateOf<String?>(null) }
    var deleteRequest by remember { mutableStateOf(false) }
    //check if the name wants to be changed
    var changeName by remember { mutableStateOf(false) }
    var changeNameTo by remember { mutableStateOf("") }
    var chosenLocation by remember { mutableStateOf<FavoriteLocation?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }

    //check for newly added to favorites
    LaunchedEffect(favoritesScreenViewModel.lastfavoriteadded,uiState.favoritesLocationMap) {
        val newname = favoritesScreenViewModel.lastfavoriteadded
        if(newname != null){
            val item = uiState.favoritesLocationMap.values.find{it.name == newname}//uiState.favoritesLocationMap[newname]//[deleteplacebyname]//[newname]
            if(item == null) return@LaunchedEffect
            //if(item != null){
                chosenLocation = item
                deletePlaceByName = item.name
                changeName = true
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
                    deletePlaceByName =favoriteLocation.name
                    chosenLocation =favoriteLocation
                    selectedName = favoriteLocation.name
                }
                    .padding(6.dp)

            ){
            FavoriteCard(
                title = favoriteLocation.name,
                subtitle = favoriteLocation.address,
                selected = selectedbox, //
                onButtonClick = {
                    homeScreenViewModel.setMarkerPosition(favoriteLocation.coordinates)
                    navHostController.navigate("graph")
                    { popUpTo("graph")
                    }
                } ,
                onButtonDelete = {
                    deleteRequest = true
                },
                onButtonChangName = {
                    changeName = true
                }
            )
        }
        }

    }
    }

    if(deleteRequest && chosenLocation !=null){
        AlertDialog(
            onDismissRequest = { deleteRequest = false },

            title = {
                Text("Delete?")
            },

            text = {
                Text("Do you wish to delete address $deletePlaceByName")
            },

            confirmButton = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    TextButton(
                        onClick = {
                            deleteRequest = false
                        }
                    ) {
                        Text(stringResource(R.string.no))
                    }

                    TextButton(
                        onClick = {
                            deletePlaceByName?.let { name ->
                                favoritesScreenViewModel.removeFavoriteLocation(name)
                            }

                            deleteRequest = false
                            chosenLocation = null
                        }
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                }
            }
        )
        /*
        AlertDialog(
            onDismissRequest = {deleteRequest= false},
            title = {Text("Delete location")},
            text = {Text("Do you wish to delete your location ${deletePlaceByName}?" )},
            confirmButton = {
                TextButton(
                    onClick = {
                       deletePlaceByName?.let { name ->
                           favoritesScreenViewModel.removeFavoriteLocation(name)
                       }
                        deleteRequest = false
                        chosenLocation =null
                    }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {deleteRequest = false}) {
                    Text("Cancel")
                }
            }
        )*/
    }

    if (changeName && chosenLocation != null) {
        AlertDialog(
            onDismissRequest = {
                changeName = false
                changeNameTo = ""
            },

            title = {
                Text("Enter new name")
            },

            text = {
                TextField(
                    value = changeNameTo,
                    onValueChange = { changeNameTo = it },
                    placeholder = {
                        Text("New name")
                    }
                )
            },

            confirmButton = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // Cancel -> left
                    TextButton(
                        onClick = {
                            changeName = false
                            changeNameTo = ""
                        }
                    ) {
                        Text("Cancel")
                    }

                    // Save -> right
                    TextButton(
                        onClick = {
                            favoritesScreenViewModel.changeFavoriteLocationName(
                                prevname = chosenLocation!!.name,
                                changenameto = changeNameTo
                            )

                            changeNameTo = ""
                            changeName = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        )
        /*
            AlertDialog(
            onDismissRequest = {
                changeName = false
                changeNameTo = ""

            },
            title = { Text("Enter new name") },
            text = {
                TextField(
                    value = changeNameTo,
                    onValueChange = { changeNameTo = it },
                    placeholder = { Text("New name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    //changenameof?.let {
                        favoritesScreenViewModel.changeFavoriteLocationName(
                            prevname = chosenLocation!!.name,//it.name,
                            changenameto = changeNameTo
                        )
                    //}
                    changeNameTo = ""
                    changeName = false

                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    changeName = false

                }) {
                    Text("Cancel")
                }
            }
        )*/
    }


}
