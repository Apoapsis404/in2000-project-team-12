package no.uio.ifi.in2000.team12.team12project.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao
import no.uio.ifi.in2000.team12.team12project.data.local.dao.NotesDao
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel

class NotesScreenViewModelFactory (private val dao: NotesDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesScreenViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}