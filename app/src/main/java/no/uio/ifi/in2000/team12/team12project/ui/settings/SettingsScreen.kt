package no.uio.ifi.in2000.team12.team12project.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.DropDown

@Composable
fun SettingsScreen(
    settingsScreenViewModel: SettingsScreenViewModel = viewModel()
) {
    val uiState = settingsScreenViewModel.uiState
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 36.dp)
    ){
        Text(
            stringResource(R.string.settingsTitle),
            fontSize = 36.sp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
        )

        val darkmodeBool = settingsScreenViewModel.uiState.collectAsState().value.lightmode
        ToggleSetting(
            stringResource(R.string.darkModeSwitch, if (darkmodeBool) "dark" else "light"),
            checked = darkmodeBool,
        ) {
            settingsScreenViewModel.toggleTheme()
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.language),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            DropDown<Languages>(
                uiState.collectAsState().value.language.name,
                modifier = Modifier.width(180.dp)
            )  {
                settingsScreenViewModel.switchLanguage(it)
            }
        }

    }

}

@Composable
fun ToggleSetting(
    text: String,
    checked: Boolean,
    onChecked: (Boolean)-> Unit
){
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
            checked = !checked,
            onCheckedChange = onChecked
        )
    }

}

@Preview(
    showBackground = true,
)
@Composable
fun SettingsScreenPreview(
){
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen()
    }
}