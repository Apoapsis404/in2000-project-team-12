package no.uio.ifi.in2000.team12.team12project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreen
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreen
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreen
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreen
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun Navigator(
    modifier: Modifier = Modifier,
    homeViewModel: HomeScreenViewModel,
    settingsViewModel: SettingsScreenViewModel,
    favoritesViewModel: FavoritesScreenViewModel,
    notesScreenViewModel: NotesScreenViewModel,
    graphScreenViewModel: GraphScreenViewModel,
    navController: NavHostController,
    onNoInternet: (Int?) -> Unit
){
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
            GraphScreen(
                graphScreenViewModel = graphScreenViewModel,
                navHostController = navController,
                onNoInternet = onNoInternet
            )
        }
        composable(route = "settings") {
            SettingsScreen(
                settingsScreenViewModel = settingsViewModel,
                navHostController = navController,
                onNoInternet = onNoInternet
            )
        }
        composable(route = "favorites") {
            FavoritesScreen(
                favoritesScreenViewModel = favoritesViewModel,
                homeScreenViewModel = homeViewModel,
                navHostController = navController,
                onNoInternet = onNoInternet
            )
        }
        composable(route = "notes") {
            NotesScreen(
                notesScreenViewModel = notesScreenViewModel,
                navHostController = navController,
                onNoInternet = onNoInternet
            )
        }
    }
}
