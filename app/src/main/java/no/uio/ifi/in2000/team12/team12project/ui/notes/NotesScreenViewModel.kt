package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.dao.NotesDao
import no.uio.ifi.in2000.team12.team12project.model.notes.Note


data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isEditing: Boolean = false,
    val selectedNote: Note? = null,
    val showDeleteDialog: Boolean = false
)

class NotesScreenViewModel(private val dao: NotesDao) : ViewModel() {
    private val _interactionState = MutableStateFlow(NotesUiState())

    val uiState: StateFlow<NotesUiState> = dao.getAllNotes()
        .combine(_interactionState) { notes, interaction ->
            interaction.copy(notes = notes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState()
        )

    // Events flow UP from UI
    fun onNoteSelected(note: Note?) {
        _interactionState.update { it.copy(selectedNote = note) }
    }

    fun setEditing(isEditing: Boolean) {
        _interactionState.update { it.copy(isEditing = isEditing) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _interactionState.update { it.copy(showDeleteDialog = show) }
    }
    fun addNote(note: Note){
        viewModelScope.launch {
            dao.insertNote(note)
            setEditing(false)
            onNoteSelected(null)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            dao.deleteNote(note)
            setShowDeleteDialog(false)
            onNoteSelected(null)
        }
    }
}