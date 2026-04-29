package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.local.dao.NotesDao
import no.uio.ifi.in2000.team12.team12project.model.notes.Note

class NotesScreenViewModel(private val dao: NotesDao) : ViewModel() {
    val uiState: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addNote(note: Note){
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
}