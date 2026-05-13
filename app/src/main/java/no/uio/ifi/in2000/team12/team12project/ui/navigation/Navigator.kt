package no.uio.ifi.in2000.team12.team12project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreen
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreen
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreen
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreen
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils

@Composable
fun Navigator(
    modifier: Modifier = Modifier,
    homeViewModel: HomeScreenViewModel,
    settingsViewModel: SettingsScreenViewModel,
    favoritesViewModel: FavoritesScreenViewModel,
    notesScreenViewModel: NotesScreenViewModel,
    graphScreenViewModel: GraphScreenViewModel,
    navController: NavHostController,
    onNoInternet: () -> Unit
){
    homeViewModel.setMarkerPosition(LatLng(59.9127, 10.7461))
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ){
        composable(route = "home") {
            HomeScreen(
                homeScreenViewModel = homeViewModel
            )
        }
        composable(route = "graph") {
            LaunchedEffect(homeViewModel.uiState.collectAsState().value.markerPosition) {
                graphScreenViewModel.setMarkerPositionAndFetch(homeViewModel.uiState.value.markerPosition)
            }
            GraphScreen(graphScreenViewModel = graphScreenViewModel)
        }
        composable(route = "settings") {
            SettingsScreen(settingsScreenViewModel = settingsViewModel)
        }
        composable(route = "favorites") {
            FavoritesScreen(favoritesScreenViewModel = favoritesViewModel,
                homeScreenViewModel = homeViewModel,
                navHostController = navController,
                onNoInternet = onNoInternet)
        }
        composable(route = "notes") {
            NotesScreen(notesScreenViewModel = notesScreenViewModel,
                navHostController = navController)
        }
    }
}