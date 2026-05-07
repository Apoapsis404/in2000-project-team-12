package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.team12.team12project.model.notes.Note
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.FavoriteCard
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.MintGreen
import no.uio.ifi.in2000.team12.team12project.ui.theme.sec2


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
                            /*
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) sec2 else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )*/
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
                    /*
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { state ->
                            if (state == SwipeToDismissBoxValue.EndToStart) {
                                onRemoveNote(note)
                                true
                            } else {
                                false
                            }
                        }
                    )*/
                    /*
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Color.Red.copy(alpha = 0.8f)
                            } else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White

                                )
                            }
                        }
                    ) {
                        NoteItem(
                            note,
                            onClick = {
                                editing = true
                                selectedNote = note
                            }, //
                            onDeleteClick = {

                            }
                        )
                    }*/
                    //

                    //
                    /*
                    NoteItem(
                        note = note,
                        /*
                        //
                        onClick = {
                            //editing = true /hmmmmm
                            selectedNote = note
                        },*/ //
                        onDeleteButtonClick = {
                            selectedNote = note
                            showDialog = true
                        },
                        onEditButtonClick = {
                            editing = true
                        }
                    )
                }*/

            }
            Button(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    editing = true
                    selectedNote = null//(content = "")
                }
            ) {
                Text("Add note")
            }
        }
    }
    if(showDialog && selectedNote != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete note") },
            text = { Text("Do wish to delete this note?") },
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
                    Text("delete")//, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("cancel")
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