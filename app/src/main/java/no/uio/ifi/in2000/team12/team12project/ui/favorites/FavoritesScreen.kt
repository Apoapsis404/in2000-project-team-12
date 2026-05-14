package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel

@Composable
fun FavoritesScreen(
    favoritesScreenViewModel: FavoritesScreenViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    navHostController: NavHostController,
    onNoInternet: (Int?) -> Unit
) {
    val context = LocalContext.current
    val uiState by favoritesScreenViewModel.uiState.collectAsState()

    BackHandler {
        if (NetworkUtils.isNetworkAvailable(context)) {
            navHostController.popBackStack()
        } else {
            onNoInternet(R.string.no_internet_map_message)
        }
    }

    FavoritesScreenContent(
        uiState = uiState,
        onNavigateToGraph = { coords ->
            if (NetworkUtils.isNetworkAvailable(context)) {
                homeScreenViewModel.setMarkerPosition(coords)
                navHostController.navigate("graph") { popUpTo("graph") }
            } else {
                onNoInternet(R.string.no_internet_navigation_message)
            }
        },
        viewModel = favoritesScreenViewModel
    )
}
@Composable
fun FavoritesScreenContent(
        uiState: FavoritesScreenUiState,
        onNavigateToGraph: (LatLng) -> Unit,
        viewModel: FavoritesScreenViewModel
    ) {
        Box(modifier = Modifier) {
            LazyColumn {
                items(uiState.favoritesLocationMap.toList()) { (key, favoriteLocation) ->
                    val isSelected = uiState.selectedLocation?.name == favoriteLocation.name

                    Box(modifier = Modifier
                        .clickable { viewModel.onLocationSelected(favoriteLocation) }
                        //.padding(6.dp)
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp)

                        //.padding(horizontal = 20.dp, vertical = 18.dp)
                        .border(
                            width = if(isSelected) 4.dp else 0.dp,
                            color = if(isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .shadow(6.dp,RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                    ) {
                        FavoriteCard(
                            title = favoriteLocation.name,
                            subtitle = favoriteLocation.address,
                            selected = isSelected,
                            onIconClick = { onNavigateToGraph(favoriteLocation.coordinates) },
                            onDeleteClick = {
                                viewModel.onLocationSelected(favoriteLocation)
                                viewModel.setShowDeleteDialog(true)
                            },
                            onEditClick = {
                                viewModel.onLocationSelected(favoriteLocation)
                                viewModel.setShowRenameDialog(true, "")
                            }
                        )
                    }
                }
            }
        }

        // Delete Dialog
        if (uiState.showDeleteDialog && uiState.selectedLocation != null) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowDeleteDialog(false) },
                title = { Text(stringResource(R.string.delete_favorite_pop_up_header)) },
                text = { Text(stringResource(R.string.delete_favorite_pop_up_body))},
                confirmButton = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) { Text(stringResource(R.string.no)) }
                        TextButton(onClick = { viewModel.confirmDelete() }) { Text(stringResource(R.string.yes)) }
                    }
                }
            )
        }

        // Rename Dialog (Handles newly added favorites automatically via State)
        if (uiState.showRenameDialog && uiState.selectedLocation != null) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowRenameDialog(false) },
                title = { Text(stringResource(R.string.favorite_new_name_header)) },
                text = {
                    TextField(
                        value = uiState.renameTextFieldValue,
                        onValueChange = { viewModel.onRenameTextChanged(it) },
                        placeholder = { Text(stringResource(R.string.favorite_new_name_placeholder)) }
                    )
                },
                confirmButton = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { viewModel.setShowRenameDialog(false) }) { Text(stringResource(R.string.delete_pop_up_cancel)) }
                        TextButton(onClick = { viewModel.confirmRename() }) { Text(stringResource(R.string.save_favorite_button)) }
                    }
                }
            )
        }
    }
