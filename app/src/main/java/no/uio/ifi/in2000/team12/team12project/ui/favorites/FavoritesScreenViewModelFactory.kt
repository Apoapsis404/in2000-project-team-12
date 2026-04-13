package no.uio.ifi.in2000.team12.team12project.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao

class FavoritesScreenViewModelFactory(private val dao: FavoriteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesScreenViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}