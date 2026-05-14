package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.team12.team12project.R
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils
import no.uio.ifi.in2000.team12.team12project.model.notes.Note

@Composable
fun NotesScreen(
    notesScreenViewModel: NotesScreenViewModel,
    navHostController: NavHostController,
    onNoInternet: (Int?) -> Unit
) {
    val uiState by notesScreenViewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler {
        if (NetworkUtils.isNetworkAvailable(context)) {
            navHostController.popBackStack()
        } else {
            onNoInternet(R.string.no_internet_map_message)
        }
    }

    NotesScreenContent(
        notes = uiState.notes,
        onAddNote = { notesScreenViewModel.addNote(it) },
        onRemoveNote = { notesScreenViewModel.deleteNote(it) },
        notesScreenViewModel = notesScreenViewModel
    )


}

@Composable
fun NotesScreenContent(
    notes: List<Note>,
    onAddNote: (Note) -> Unit,
    onRemoveNote: (Note) -> Unit,
    notesScreenViewModel: NotesScreenViewModel = viewModel()
) {
    val uiState by notesScreenViewModel.uiState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        if (uiState.isEditing) {
            NoteEntryField(
                note = uiState.selectedNote,
                onSave = { updatedText ->

                    val noteToSave = uiState.selectedNote?.copy(content = updatedText)
                        ?: Note(content = updatedText)

                    onAddNote(noteToSave)

                },
                onCancel = {
                    notesScreenViewModel.setEditing(false)
                    notesScreenViewModel.onNoteSelected(null)
                }
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    notes,
                    key = { it.id ?: it.hashCode() }
                ) { note ->
                    val isSelected = uiState.selectedNote?.id == note.id

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)

                            .border(
                                width = if(isSelected) 4.dp else 0.dp,
                                color = if(isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .shadow(6.dp,RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                    ){
                        Notecard(
                            note = note,
                            selected = isSelected,

                            onClick = {
                                notesScreenViewModel.onNoteSelected(note)
                            },

                            onDeleteButtonClick = {
                                notesScreenViewModel.onNoteSelected(note)
                                notesScreenViewModel.setShowDeleteDialog(true)
                            },

                            onEditButtonClick = {
                                notesScreenViewModel.onNoteSelected(note)
                                notesScreenViewModel.setEditing(true)
                            }
                        )
                    }
                    }


            }

            Button(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    notesScreenViewModel.onNoteSelected(null)
                    notesScreenViewModel.setEditing(true)
                }
            ) {
                Text(stringResource(R.string.add_note))
            }
        }
    }
    //delete dialog popup
    if(uiState.showDeleteDialog && uiState.selectedNote != null) {
        AlertDialog(
            onDismissRequest = {
                notesScreenViewModel.setShowDeleteDialog(false)

            },

            title = {
                Text(stringResource(R.string.delete_note_pop_up_header))
            },

            text = {
                Text(stringResource(R.string.delete_note_pop_up_body))
            },

            confirmButton = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    TextButton(
                        onClick = {
                            notesScreenViewModel.setShowDeleteDialog(false)
                        }
                    ) {
                        Text(stringResource(R.string.no))
                    }


                    TextButton(
                        onClick = {
                            uiState.selectedNote?.let {
                                onRemoveNote(it)
                            }

                        }
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                }
            }
        )

    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    val mockNotes = listOf(
        Note(id = 1, content = "First Note\nThis is the content of the first note."),
        Note(id = 2, content = "Shopping List\nMilk, Eggs, Bread."),
        Note(id = 3, content = "Ideas\nBuild a cool Android app!")
    )

    NotesScreenContent(
        notes = mockNotes,
        onAddNote = {},
        onRemoveNote = {}
    )
}