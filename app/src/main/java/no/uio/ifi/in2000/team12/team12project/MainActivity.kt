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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.Header
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.NavigationDrawer
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.navigation.Navigator
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.notes.NotesScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.settings.SettingsScreenViewModelFactory
import no.uio.ifi.in2000.team12.team12project.ui.theme.Team12ProjectTheme
import no.uio.ifi.in2000.team12.team12project.data.local.SettingsEntity
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages
import java.util.Locale


class MainActivity : ComponentActivity() {

    // For local language change
    override fun attachBaseContext(newBase: Context) {
        // Read saved language from SharedPreferences (Room is async, can't use it here)
        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)

        // Try to get saved language, fallback to system language if not set (first run)
        val systemLocale = Locale.getDefault().language
        val defaultLang = if (systemLocale.startsWith("nb") || systemLocale.startsWith("nn") || systemLocale.startsWith("no")) "nb" else "en"
        val langCode = prefs.getString("language", defaultLang) ?: defaultLang

        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }


    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as Team12Application
        val settingsDao = app.database.settingsDao()

        // Load settings or initialize if null (first run)
        val settings = runBlocking { settingsDao.getSettings().first() }

        if (settings == null) {
            // Detect system language
            val systemLocale = Locale.getDefault().language
            val defaultLanguage = if (systemLocale.startsWith("nb") || systemLocale.startsWith("nn") || systemLocale.startsWith("no")) {
                Languages.NORSK
            } else {
                Languages.ENGLISH
            }

            // Detect system theme (Dark Mode)
            val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            val initialSettings = SettingsEntity(
                lightMode = !isSystemDark,
                language = defaultLanguage
            )

            // Save initial settings to database
            runBlocking {
                settingsDao.saveSettings(initialSettings)
            }
            setLocale(defaultLanguage.name)
        } else {
            setLocale(settings.language.name)
        }

        @Suppress("DEPRECATION")
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY, Locale.getDefault())
        }
        enableEdgeToEdge()
        setContent {
            val graphScreenViewModel : GraphScreenViewModel = viewModel(factory = GraphScreenViewModelFactory(app.climateDataRepository, app.plantsRepository))
            val settingsViewModel : SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(app.database.settingsDao()))
            val favoritesViewModel : FavoritesScreenViewModel = viewModel(factory = FavoritesScreenViewModelFactory(app.database.favoritesDao()))
            val notesScreenViewModel : NotesScreenViewModel = viewModel(factory = NotesScreenViewModelFactory(app.database.notesDao()))
            val homeScreenViewModel : HomeScreenViewModel = viewModel()

            Team12ProjectTheme(
                darkTheme = !settingsViewModel.uiState.collectAsState().value.lightMode,
                dynamicColor = false
            ) {
                MainComp(
                    settingsViewModel,
                    favoritesViewModel,
                    notesScreenViewModel,
                    homeScreenViewModel,
                    graphScreenViewModel,
                )
            }
        }
    }



    private fun setLocale(language: String) {
        val languageCode = when (language) {
            "ENGLISH" -> "en"
            "NORSK" -> "nb"
            else -> language.lowercase()
        }
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit(commit = false) { putString("language", languageCode) }
    }
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
    var showFavoritesAlertDialog by remember { mutableStateOf(false)}

    val context = LocalContext.current
    var showStartupNoInternetDialog by remember { mutableStateOf(!NetworkUtils.isNetworkAvailable(context)) }
    val showNavigationNoInternetDialog = remember { mutableStateOf(false) }
    var noInternetMessageResId by remember { mutableIntStateOf(R.string.no_internet_navigation_message) }

    // Dialog for no internet at startup
    if (showStartupNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismissal */ },
            title = { Text(stringResource(R.string.no_internet_title)) },
            text = { Text(stringResource(R.string.no_internet_startup_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        (context as? MainActivity)?.finish()
                    }
                ) {
                    Text(stringResource(R.string.exit_app))
                }
            }
        )
    }

    // Dialog for no internet when navigating
    if (showNavigationNoInternetDialog.value) {
        AlertDialog(
            onDismissRequest = { 
                showNavigationNoInternetDialog.value = false
            },
            title = { Text(stringResource(R.string.no_internet_title)) },
            text = { Text(stringResource(noInternetMessageResId)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNavigationNoInternetDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            NavigationDrawer(
                onNavigate = {route -> navController.navigate(route = route)},
                onClose = {scope.launch { drawerState.close() }},
                onNoInternet = { messageResId ->
                    noInternetMessageResId = messageResId ?: R.string.no_internet_navigation_message
                    showNavigationNoInternetDialog.value = true 
                }
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
                        showFavoritesAlertDialog = true
                    },
                    onNoInternet = { messageResId ->
                        noInternetMessageResId = messageResId ?: R.string.no_internet_navigation_message
                        showNavigationNoInternetDialog.value = true 
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
                onNoInternet = { messageResId ->
                    noInternetMessageResId = messageResId ?: R.string.no_internet_navigation_message
                    showNavigationNoInternetDialog.value = true
                }
            )

        }
        if(showFavoritesAlertDialog){
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
                                val currentRoute = navController.currentBackStackEntry?.destination?.route

                                if (currentRoute != "favorites") {
                                    navController.navigate("favorites")
                                }
                                favoriteScreenViewModel.addFavoriteLocation(
                                    location = homeScreenViewModel.uiState.value.markerPosition,//askpermissonforlatlng!!,
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

