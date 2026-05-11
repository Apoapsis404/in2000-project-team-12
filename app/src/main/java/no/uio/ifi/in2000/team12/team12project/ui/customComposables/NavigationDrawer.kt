package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.R

@Composable
fun NavigationDrawer(
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
) {
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
        selectedTextColor = MaterialTheme.colorScheme.onPrimary
    )
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
                        onClose()
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
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.home),
                fontWeight = Medium,
                fontSize = 18.sp
            ) },
            selected = false,
            onClick = {
               onNavigate("home")
                onClose()
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
                onNavigate("graph")
                onClose()
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
                onNavigate("favorites")
                onClose()
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
                onNavigate("notes")
                onClose()
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
                onNavigate("settings")
                onClose()
            },
            colors = drawerItemColors

        )
    }
}