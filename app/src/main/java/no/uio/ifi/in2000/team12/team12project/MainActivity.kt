package no.uio.ifi.in2000.team12.team12project

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameAndAddress
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
            val graphScreenViewModel : GraphScreenViewModel = viewModel(
                factory = GraphScreenViewModelFactory(app.climateDataRepository, app.plantsRepository)
            )
            val settingsViewModel : SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(app.database.settingsDao()))
            val favoritesViewModel : FavoritesScreenViewModel = viewModel(factory = FavoritesScreenViewModelFactory(app.database.favoritesDao()))
            val notesScreenViewModel : NotesScreenViewModel = viewModel(factory = NotesScreenViewModelFactory(app.database.notesDao()))
            val homeScreenViewModel : HomeScreenViewModel = viewModel()
            //HomeScreen()
            //GraphScreen()
            Team12ProjectTheme(
                darkTheme = !settingsViewModel.uiState.collectAsState().value.lightmode,
                dynamicColor = false
            ) {
                MainComp(settingsViewModel, favoritesViewModel, notesScreenViewModel, homeScreenViewModel, graphScreenViewModel)
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        val app = applicationContext as Team12Application
//        app.database.close()
//    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainComp(
    settingsScreenViewModel: SettingsScreenViewModel,
    favoriteScreenViewModel: FavoritesScreenViewModel,
    notesScreenViewModel: NotesScreenViewModel,
    homeScreenViewModel: HomeScreenViewModel,
    graphScreenViewModel: GraphScreenViewModel,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    //variables with the changed location on the map and to ask if user wants to add location to favorites
    var askpermissonforlatlng by remember { mutableStateOf<LatLng?>(null)}
    var askforpermission by remember { mutableStateOf(false)}

    val context = LocalContext.current

    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
        selectedTextColor = MaterialTheme.colorScheme.onPrimary
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.primary,
                drawerContentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Row {
                    Text(stringResource(R.string.menu),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 24.sp,
                        fontWeight = Bold)

                    //spacer
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                scope.launch { drawerState.close() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cont_desc_exit),
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.home),
                        fontWeight = Medium,
                        fontSize = 18.sp
                    ) },
                    selected = false,
                    onClick = {
                        navController.navigate("home")
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.climate_graph),
                        fontWeight = Medium,
                        fontSize = 18.sp
                    ) },
                    selected = false,
                    onClick = {
                        navController.navigate("graph")
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.favorites),
                        fontWeight = Medium,
                        fontSize = 18.sp
                    ) },
                    selected = false,
                    onClick = {
                        navController.navigate("favorites")
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.notes),
                        fontWeight = Medium,
                        fontSize = 18.sp
                    ) },
                    selected = false,
                    onClick = {
                        navController.navigate("notes")
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings),
                        fontWeight = Medium,
                        fontSize = 18.sp
                    ) },
                    selected = false,
                    onClick = {
                        navController.navigate("settings")
                        scope.launch { drawerState.close() }
                    },
                    colors = drawerItemColors

                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Header(
                    onClick = { scope.launch { drawerState.open() } },
                    homeScreenViewModel = homeScreenViewModel,
                    navHostController = navController,
                    onFavoriteButtonClick = {

                        askpermissonforlatlng =
                            homeScreenViewModel.uiState.value.markerPosition

                        askforpermission = true
                    }
                )
            },
        ) { innerPadding ->
            Navigator(
                settingsViewModel = settingsScreenViewModel,
                favoritesViewModel = favoriteScreenViewModel,
                notesScreenViewModel = notesScreenViewModel,
                homeViewModel = homeScreenViewModel,
                graphScreenViewModel = graphScreenViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                navController = navController,
            )

        }
        if(askforpermission && askpermissonforlatlng != null){
            AlertDialog(
                onDismissRequest = {
                    askforpermission = false
                },

                title = {
                    Text(stringResource(R.string.favorites_popup_title))
                },

                text = {
                    Text(stringResource(R.string.favorites_popup_question))
                },

                confirmButton = {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        TextButton(
                            onClick = {
                                askforpermission = false
                            }
                        ) {
                            Text(stringResource(R.string.no))
                        }

                        TextButton(
                            onClick = {
                                scope.launch {

                                    val tempname = "temp_${System.currentTimeMillis()}"

                                    val (_, address) = getNameAndAddress(
                                        context,
                                        askpermissonforlatlng!!.latitude,
                                        askpermissonforlatlng!!.longitude
                                    )

                                    navController.navigate("favorites")

                                    favoriteScreenViewModel.addFavoriteLocation(
                                        location = askpermissonforlatlng!!,
                                        name = tempname,
                                        address = address.toString()
                                    )
                                }

                                askforpermission = false
                            }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    }
                }
            )
            /*AlertDialog(
                onDismissRequest = {askforpermission = false},
                title = {Text(stringResource(R.string.favorites_popup_title))},
                text = {Text(stringResource(R.string.favorites_popup_question))},
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val tempname = "temp_${System.currentTimeMillis()}" //temporary name so it is easier to tell apart the diffrent adresses in order to rename

                                val (name, address) = getNameandAdress( //name is no longer used
                                    context,
                                    askpermissonforlatlng!!.latitude,
                                    askpermissonforlatlng!!.longitude
                                )
                                //navigates to favorite screen
                                navController.navigate("favorites")

                                favoriteScreenViewModel.addFavoriteLocation(
                                    location = askpermissonforlatlng!!,
                                    name = tempname , //"add name",//name.toString(), //add apart of the address. will later on add ability to change the name
                                    address = address.toString()
                                )

                            }
                            askforpermission = false
                        }) {
                        Text(stringResource(R.string.Yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {askforpermission = false}) {
                        Text(stringResource(R.string.no))
                    }
                }
            )*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel,
    navHostController: NavHostController,
    onFavoriteButtonClick: () -> Unit
) {
    val titleColors = MaterialTheme.colorScheme.onPrimary
    val context = LocalContext.current
    val addressTitle = homeScreenViewModel.uiState.collectAsState().value.address

    val navStackBackEntry by navHostController.currentBackStackEntryAsState()
    val currentRoute = navStackBackEntry?.destination?.route

    var showPopup by remember { mutableStateOf(false) }

    val uiState by homeScreenViewModel.uiState.collectAsState()
    LaunchedEffect(uiState.markerPosition) {
        val (_, address) = getNameAndAddress(
            context,
            uiState.markerPosition.latitude,
            uiState.markerPosition.longitude
        )
        homeScreenViewModel.updateAddress(address.toString())
    }

    TopAppBar(
        {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Icon(
//                    imageVector = Icons.Default.LocationOn,
//                    contentDescription = "location symbol",
//                    tint = MaterialTheme.colorScheme.onPrimary
//                )
                Text(
                    //stringResource(R.string.app_title),
                    text = addressTitle,
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
            TopBarIcon(
                onClick = onFavoriteButtonClick,
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite"
            )

            if(currentRoute == "graph"){
                //Map symbol
                TopBarIcon({navHostController.navigate(route = "home")}, Icons.Outlined.Map, "Navigate to map")

            }else {
                //Graph symbol
                TopBarIcon({ navHostController.navigate("graph") }, Icons.Outlined.DataThresholding, "menu")
            }
            //Information symbol
            TopBarIcon({showPopup = true}, Icons.Outlined.Info, "Information")

            //favorite symbol


        }

    )
    val infoPopupState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if(showPopup) {
        // Modal bottom sheet for info button
        ModalBottomSheet(
            onDismissRequest = { showPopup = false},
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



@Preview
@Composable
fun HeaderPreview(modifier: Modifier = Modifier.fillMaxSize()){
    //Header() {}
}