package no.uio.ifi.in2000.team12.team12project

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.InfoPopup
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.NavigationDrawer
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameandAdress
import no.uio.ifi.in2000.team12.team12project.ui.navigation.Navigator
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.theme.Team12ProjectTheme
import java.util.Locale


class MainActivity : ComponentActivity() {

    // For local language change
    override fun attachBaseContext(newBase: Context) {
        // Read saved language from SharedPreferences (Room is async, can't use it here)
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val langCode = prefs.getString("language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }



    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as Team12Application
            val settingsViewModel : SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(app.database.settingDao()))
            val favoritesViewModel : FavoritesScreenViewModel = viewModel(factory = FavoritesScreenViewModelFactory(app.database.favoriteDao()))
            val notesScreenViewModel : NotesScreenViewModel = viewModel(factory = NotesScreenViewModelFactory(app.database.notesDao()))
            val homeScreenViewModel : HomeScreenViewModel = viewModel()

            Team12ProjectTheme(
                darkTheme = !settingsViewModel.uiState.collectAsState().value.lightmode,
                dynamicColor = false
            ) {
                MainComp(
                    settingsViewModel,
                    favoritesViewModel,
                    notesScreenViewModel,
                    homeScreenViewModel,
                )
            }
        }
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainComp(
    settingsScreenViewModel: SettingsScreenViewModel,
    favoriteScreenViewModel: FavoritesScreenViewModel,
    notesScreenViewModel: NotesScreenViewModel,
    homeScreenViewModel: HomeScreenViewModel,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    //variables with the changed location on the map and to ask if user wants to add location to favorites
    var askpermissonforlatlng by remember { mutableStateOf<LatLng?>(null)}
    var showFavoritesAlertDialog by remember { mutableStateOf(false)}

    val context = LocalContext.current



    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            NavigationDrawer(
                onNavigate = {route -> navController.navigate(route = route)},
                onClose = {scope.launch { drawerState.close() }}
            )
        }
    ) {
        Scaffold(
            topBar = {
                Header(
                    onClick = { scope.launch { drawerState.open() } },
                    homeScreenViewModel = homeScreenViewModel,
                    navHostController = navController,
                    onFavoriteButtonClick = {
                        askpermissonforlatlng = homeScreenViewModel.uiState.value.markerPosition
                        showFavoritesAlertDialog = true
                    }
                )
            },
        ) { innerPadding ->
            Navigator(
                settingsViewModel = settingsScreenViewModel,
                favoritesViewModel = favoriteScreenViewModel,
                notesScreenViewModel = notesScreenViewModel,
                homeViewModel = homeScreenViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()), navController = navController
            )

        }
        if(showFavoritesAlertDialog && askpermissonforlatlng != null){
            AlertDialog(
                onDismissRequest = { showFavoritesAlertDialog = false },
                title = { Text(stringResource(R.string.favorites_popup_title)) },
                text = { Text(stringResource(R.string.favorites_popup_question)) },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Decline button
                        TextButton(
                            onClick = { showFavoritesAlertDialog = false }
                        ) {
                            Text(stringResource(R.string.no))
                        }
                        // Confirm button
                        TextButton(
                            onClick = {
                                //GAMMEL (FØR ADRESSE I HOMSCREENVIEWMODEL)

//                                scope.launch {
//                                    val tempname = "temp_${System.currentTimeMillis()}"
//
//                                    val (name, address) = getNameandAdress(
//                                        context,
//                                        askpermissonforlatlng!!.latitude,
//                                        askpermissonforlatlng!!.longitude
//                                    )
//
//                                    navController.navigate("favorites")
//
//                                    favoriteScreenViewModel.addFavoriteLocation(
//                                        location = askpermissonforlatlng!!,
//                                        name = tempname,
//                                        address = address.toString()
//                                    )
//                                }
//
//                                showFavoritesAlertDialog = false

                            // NY
                                // Trenger ikke oversette addresse lenger, da den allerede er lagret i homeScreenViewModel
                                navController.navigate("favorites")

                                favoriteScreenViewModel.addFavoriteLocation(
                                    location = askpermissonforlatlng!!,
                                    name = "temp_${System.currentTimeMillis()}",
                                    address = homeScreenViewModel.uiState.value.address
                                )
                                showFavoritesAlertDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    }
                }
            )
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel,
    navHostController: NavHostController,
    onFavoriteButtonClick: () -> Unit
){
    val titleColors = MaterialTheme.colorScheme.onPrimary
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentAddress = homeScreenViewModel.uiState.collectAsState().value.address

    val navStackBackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navStackBackEntry?.destination?.route

    var showInfoPopup by remember { mutableStateOf(false) }
    val infoPopupState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    TopAppBar(
        {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentAddress,
                    color = titleColors,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

            }
        },
        modifier = modifier
            .fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.cont_desc_menu),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = Color.Blue,
            navigationIconContentColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = Color.DarkGray,
            actionIconContentColor = Color.Magenta,
            subtitleContentColor = Color.DarkGray,

        ),
        actions = {
            //favorite symbol
            TopBarIcon(
                onClick = onFavoriteButtonClick,
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite"
            )

            if(currentRoute == "graph"){
                //Map symbol
                TopBarIcon(
                    { navHostController.navigate(route = "home") },
                    Icons.Outlined.Map,
                    "Navigate to map"
                )

            }else {
                //Graph symbol
                TopBarIcon(
                    { navHostController.navigate("graph") },
                    Icons.Outlined.DataThresholding,
                    "menu"
                )
            }
            //Information symbol
            TopBarIcon(
                {showInfoPopup = true},
                Icons.Outlined.Info,
                "Information"
            )
        }
    )

    if(showInfoPopup) {
        // Modal bottom sheet for info button
        ModalBottomSheet(
            onDismissRequest = { showInfoPopup = false},
            sheetState = infoPopupState,
            containerColor = MaterialTheme.colorScheme.background,

            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            InfoPopup(currentRoute ?: "home")
        }
    }
}

@Composable
fun TopBarIcon(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String
){
    IconButton(onClick = onClick){
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}