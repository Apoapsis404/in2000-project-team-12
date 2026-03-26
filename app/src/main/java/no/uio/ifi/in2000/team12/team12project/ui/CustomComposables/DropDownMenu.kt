package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.enums.enumEntries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified E : Enum<E>> DropDown(defaultName: String, modifier: Modifier = Modifier, crossinline onClick : (E) -> Unit) {

    val options = enumEntries<E>()
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(defaultName)

    ExposedDropdownMenuBox (
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.heightIn(max = 280.dp),
    ) {
        TextField(
            modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option.name)},
                    onClick = {
                        onClick(option)
                        textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                        expanded = false
                    },

                    )
            }
        }
    }
}

