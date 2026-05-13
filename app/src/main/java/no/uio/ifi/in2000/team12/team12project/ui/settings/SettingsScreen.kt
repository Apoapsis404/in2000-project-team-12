package no.uio.ifi.in2000.team12.team12project.ui.settings

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.team12.team12project.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.DropDown

@Composable
fun SettingsScreen(
    settingsScreenViewModel: SettingsScreenViewModel = viewModel(),
    navHostController: NavHostController,
    onNoInternet: (Int?) -> Unit
) {
    val uiState by settingsScreenViewModel.uiState.collectAsState()
    val activity = LocalActivity.current as? Activity
    val context = LocalContext.current

    BackHandler {
        if (NetworkUtils.isNetworkAvailable(context)) {
            navHostController.popBackStack()
        } else {
            onNoInternet(R.string.no_internet_map_message)
        }
    }

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
    ){
        Text(
            stringResource(R.string.settingsTitle),
            fontSize = 36.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        // Switching themes
        val theme = if(uiState.lightMode) {
            stringResource(R.string.light_mode)
        } else {
            stringResource(R.string.dark_mode)
        }
        ToggleSetting(
            stringResource(R.string.darkModeSwitch, theme),
            checked = uiState.lightMode,
            onChecked = {
                settingsScreenViewModel.toggleTheme()
            }
        )

        // Switching languages
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.language),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            DropDown<Languages>(
                defaultName = uiState.language.name,
                modifier = Modifier.width(180.dp),
                onClick = { selectedLanguage ->
                    settingsScreenViewModel.switchLanguage(selectedLanguage)
                    activity?.recreate()
                }
            )
        }
    }
}

// Gjenbrukbar metode som gir oss en rad med text label og switch.
@Composable
fun ToggleSetting(
    text: String,
    checked: Boolean,
    onChecked: (Boolean)-> Unit
){
    val switchDescription = stringResource(R.string.darkModeSwitch, text)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
    ) {
        Text(
            text,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            modifier = Modifier.semantics{
                contentDescription = switchDescription
            },
            checked = !checked,
            onCheckedChange = onChecked
        )
    }
}
