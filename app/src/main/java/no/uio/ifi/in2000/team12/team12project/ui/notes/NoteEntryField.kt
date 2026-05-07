package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import no.uio.ifi.in2000.team12.team12project.model.notes.Note

@Composable
fun NoteEntryField(
    modifier: Modifier = Modifier,
    maxChars: Int = 200,
    note: Note? = null,
    onSave: (String) -> Unit,
    onCancel: () -> Unit = {}

) {
    var text by remember { mutableStateOf(note?.content ?: "") }

    Column {
        TextField(
            value = text,
            onValueChange = { newText ->
                if (newText.length <= maxChars){
                    text = newText
                }
            },
            modifier = modifier.fillMaxWidth(),
            label = { Text("Write your note...") },
            supportingText = {
                Text(
                    text = "${text.length} / $maxChars",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                modifier = Modifier
                    .padding(top = 8.dp),
                onClick = { onSave(text) },
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                modifier = Modifier
                    .padding(top = 8.dp)
                    ,
                onClick = { onCancel() },
            ) {
                Text("Cancel")
            }
        }

    }
}

@Preview(showBackground = true, name = "Note Entry Field Preview")
@Composable
fun NoteEntryFieldPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Previewing with a 100 character limit
            NoteEntryField(maxChars = 100, onSave = {}, onCancel = {})
        }
    }
}
