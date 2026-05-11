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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.notes.Note
@Composable
fun NotesScreen(notesScreenViewModel: NotesScreenViewModel, navHostController: NavHostController) {
    val uiState by notesScreenViewModel.uiState.collectAsState()

    NotesScreenContent(
        uiState = uiState,
        onSelectNote = notesScreenViewModel::onSelectNote,
        onEditNote = { notesScreenViewModel.setEditing(true, it) },
        onAddNote = { notesScreenViewModel.setEditing(true, null) },
        onCancelEdit = { notesScreenViewModel.setEditing(false) },
        onSaveNote = notesScreenViewModel::saveNote,
        onDeleteClick = { notesScreenViewModel.setShowDeleteDialog(true, it) },
        onConfirmDelete = notesScreenViewModel::confirmDelete,
        onDismissDelete = { notesScreenViewModel.setShowDeleteDialog(false) }
    )
}
@Composable
fun NotesScreenContent(
    uiState: NotesUiState,
    onSelectNote: (Note) -> Unit,
    onEditNote: (Note) -> Unit,
    onAddNote: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveNote: (String) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit

) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isEditing) {
            NoteEntryField(
                note = uiState.selectedNote,
                onSave = onSaveNote,
                onCancel = onCancelEdit
            )

        } else {
            Column {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.notes, key = { it.id ?: it.hashCode() }) { note ->
                        NoteItem(
                            note = note,
                            selected = uiState.selectedNote?.id == note.id,
                            onClick = { onSelectNote(note) },
                            onDeleteButtonClick = { onDeleteClick(note) },
                            onEditButtonClick = { onEditNote(note) }
                        )
                    }

                }

                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = onAddNote //{ onAddNote
                    /*
                    selectedNote = null
                    isEditing = true
                    */
                    //}
                ) {
                    Text(stringResource(R.string.add_note))
                }
            }
        }

        if (uiState.showDeleteDialog) {
            DeleteNoteDialog(
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete //{ showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun DeleteNoteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_pop_up_header)) },
        text = { Text(stringResource(R.string.delete_pop_up_body)) },
        confirmButton = {
            // Keeping your custom Row layout exactly as requested
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.no))
                }
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.yes))
                }
            }
        }
    )
}
/*
@Composable
fun NotesScreen(notesScreenViewModel: NotesScreenViewModel, navHostController: NavHostController) {
    val uiState by notesScreenViewModel.uiState.collectAsState()

    NotesScreenContent(
        notes = uiState,
        onAddNote = { notesScreenViewModel.addNote(it) },
        onRemoveNote = { notesScreenViewModel.deleteNote(it) },
    )


}

@Composable
fun NotesScreenContent(
    notes: List<Note>,
    onAddNote: (Note) -> Unit,
    onRemoveNote: (Note) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    Column(modifier = Modifier
        .fillMaxSize()
    ) {
        if (editing) {
            NoteEntryField(
                note = selectedNote,
                /*
                onSave = { updatedText ->
                    val noteToSave = selectedNote?.copy(content = updatedText) ?: Note(content = updatedText)
                    onAddNote(noteToSave)
                    editing = false
                }*/
                onSave = { updatedText ->

                    val noteToSave =
                        if (selectedNote != null) {
                            selectedNote!!.copy(content = updatedText)
                        } else {
                            Note(content = updatedText)
                        }

                    onAddNote(noteToSave)

                    selectedNote = null
                    editing = false
                },
                onCancel = {
                    selectedNote = null
                    editing = false
                }
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    notes,
                    key = { it.id ?: it.hashCode() }
                ) { note ->
                    val isSelected = selectedNote?.id == note.id

                    Box(
                        modifier = Modifier
                            .padding(6.dp)

                    ){
                        NoteItem(
                            note = note,

                            selected = isSelected,

                            onClick = {
                                selectedNote = note
                            },

                            onDeleteButtonClick = {
                                selectedNote = note
                                showDialog = true
                            },

                            onEditButtonClick = {
                                selectedNote = note
                                editing = true
                            }
                        )
                    }
                    }


            }
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    editing = true
                    selectedNote = null//(content = "")
                }
            ) {
                Text(stringResource(R.string.add_note))
            }
        }
    }
    if(showDialog && selectedNote != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },

            title = {
                Text("Delete note")
            },

            text = {
                Text("Do wish to delete this note?")
            },

            confirmButton = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // NO -> left
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.no))
                    }

                    // YES -> right
                    TextButton(
                        onClick = {
                            selectedNote?.let {
                                onRemoveNote(it)
                            }

                            selectedNote = null
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.yes))
                    }
                }
            }
        )
        /*
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.delete_pop_up_header)) },
            text = { Text(stringResource(R.string.delete_pop_up_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        //onRemoveNote(selectedNote!!)
                        selectedNote?.let{
                            onRemoveNote(it)
                        }
                        selectedNote = null
                        showDialog = false
                    }
                ){
                    Text(stringResource(R.string.delete_pop_up_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete_pop_up_cancel))
                }
            }
        )*/
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
}*/