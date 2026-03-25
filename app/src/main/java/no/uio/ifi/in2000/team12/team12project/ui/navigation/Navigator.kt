package no.uio.ifi.in2000.team12.team12project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreen
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreen
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreen
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreen
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModel

@Composable
fun Navigator(
    modifier: Modifier = Modifier,
    homeViewModel: HomeScreenViewModel = viewModel(),
    graphViewModel: GraphScreenViewModel = viewModel(),
    settingsViewModel: SettingsScreenViewModel = viewModel(),
    favoritesViewModel: FavoritesScreenViewModel = viewModel(),
    navController: NavHostController,
){
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ){
        composable(
            route = "home"
        ) {
            HomeScreen(homeViewModel)
        }
        composable(
            route = "graph"
        ) {
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
            FavoritesScreen(favoritesScreenViewModel = favoritesViewModel)
        }
    }
}