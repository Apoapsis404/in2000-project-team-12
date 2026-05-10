package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.ui.home.HomeScreenViewModel

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