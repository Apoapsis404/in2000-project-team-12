package no.uio.ifi.in2000.team12.team12project.ui.favoritescreen

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.uio.ifi.in2000.team12.team12project.data.local.FavoriteLocationEntity
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenUiState
import no.uio.ifi.in2000.team12.team12project.model.favorites.FavoriteLocation
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Fake implementation of the [FavoriteDao] for testing.
 * This allows testing the ViewModel without a real Room database.
 */
class FakeFavoriteDao : FavoriteDao {
    private val _favorites = MutableStateFlow<List<FavoriteLocationEntity>>(emptyList())

    override fun getAllFavorites(): Flow<List<FavoriteLocationEntity>> = _favorites

    override suspend fun upsertFavorite(favorite: FavoriteLocationEntity) {
        val current = _favorites.value.toMutableList()
        current.removeAll { it.name == favorite.name }
        current.add(favorite)
        _favorites.value = current
    }

    override suspend fun deleteByName(name: String) {
        val current = _favorites.value.toMutableList()
        current.removeAll { it.name == name }
        _favorites.value = current
    }

    override suspend fun changeFavoriteLocationName(prevname: String, changenameto: String) {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.name == prevname }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(name = changenameto)
            _favorites.value = current
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestFavoriteScreenViewModel {

    private lateinit var viewModel: FavoritesScreenViewModel
    private lateinit var dao: FakeFavoriteDao
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        // Set the Main dispatcher to our test dispatcher
        Dispatchers.setMain(testDispatcher)
        dao = FakeFavoriteDao()
        viewModel = FavoritesScreenViewModel(dao)
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun testAddFavoriteLocation() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")

        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))

        viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location2", "Test Address2")
        viewModel.addFavoriteLocation(LatLng(5.0, 6.0), "Test Location3", "Test Address3")

        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location2"))
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location3"))
    }

    @Test
    fun testRemoveFavoriteLocation() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")
        viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location2", "Test Address2")

        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))
        viewModel.onLocationSelected(viewModel.uiState.value.favoritesLocationMap["Test Location1"])

        viewModel.confirmDelete()
        assertFalse(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location2"))
    }

    @Test
    fun testChangeFavoriteLocationName() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Old Name", "Address")
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Old Name"))

        viewModel.onLocationSelected(viewModel.uiState.value.favoritesLocationMap["Old Name"])
        viewModel.setShowRenameDialog(true, "Old Name")
        viewModel.onRenameTextChanged("New Name")
        viewModel.confirmRename()

        assertFalse(viewModel.uiState.value.favoritesLocationMap.containsKey("Old Name"))
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("New Name"))
    }

    @Test
    fun testAddDuplicateFavoriteLocation_overwrites() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        // Since the current implementation uses @Upsert, adding a duplicate name overwrites it.
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")
        assertTrue(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))

        viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location1", "Test Address2")
        
        val favorite = viewModel.uiState.value.favoritesLocationMap["Test Location1"]
        assertEquals(favorite?.address, "Test Address2")
        assertEquals(favorite?.coordinates?.latitude, 3.0)
    }
}
