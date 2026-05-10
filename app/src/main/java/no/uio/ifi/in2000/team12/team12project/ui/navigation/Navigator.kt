package no.uio.ifi.in2000.team12.team12project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun Navigator(
    modifier: Modifier = Modifier,
    homeViewModel: HomeScreenViewModel,// = viewModel(),
    settingsViewModel: SettingsScreenViewModel,
    favoritesViewModel: FavoritesScreenViewModel = viewModel(),
    notesScreenViewModel: NotesScreenViewModel,
    navController: NavHostController,
){

    // Creating a view model for the graph screen with context for plants repository
    val context = LocalContext.current
    val graphViewModel: GraphScreenViewModel = viewModel(
        factory = GraphScreenViewModelFactory(
            ClimateDataRepository(),
            PlantsRepository(context)
        )
    )


    homeViewModel.setMarkerPosition(LatLng(59.9127, 10.7461))
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ){
        composable(
            route = "home"
        ) {
            HomeScreen(
                homeViewModel,
                favoritesScreenViewModel = favoritesViewModel,
                navController,
                modifier = Modifier
            )
        }
        composable(route = "graph") {
            LaunchedEffect(homeViewModel.getMarkerPosition()) {
                graphViewModel.setMarkerPositionAndFetch(homeViewModel.getMarkerPosition())
            }
            GraphScreen(graphScreenViewModel = graphViewModel)
        }
        composable  (
            route = "settings"
        ){
            SettingsScreen(settingsScreenViewModel = settingsViewModel)
        }
        composable  (
            route = "favorites"
        ){
            FavoritesScreen(favoritesScreenViewModel = favoritesViewModel, graphViewModel,homeViewModel, navController)
        }
        composable (
            route = "notes",
        ){
            NotesScreen(notesScreenViewModel = notesScreenViewModel, navController)
        }
    }
}