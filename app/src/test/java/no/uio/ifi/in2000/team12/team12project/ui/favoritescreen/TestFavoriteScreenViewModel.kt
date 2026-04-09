package no.uio.ifi.in2000.team12.team12project.ui.favoritescreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import com.google.android.gms.maps.model.LatLng
import org.junit.Test
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import kotlin.test.assertFailsWith
import kotlin.test.fail

class TestFavoriteScreenViewModel {

    @Test
    fun testAddFavoriteLocation() = runTest {
        val viewModel = FavoritesScreenViewModel()
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")
        assert(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))

        viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location2", "Test Address2")
        viewModel.addFavoriteLocation(LatLng(5.0, 6.0), "Test Location3", "Test Address3")

        // Making sure that the locations is still in the map
        assert(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))

        assert(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location2"))
        assert(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location3"))
    }

    @Test
    fun testAddDuplicateFavoriteLocation() = runTest {
        val viewModel = FavoritesScreenViewModel()
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")

        // Making sure that the location got added to the map
        assert(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1"))

        assertFailsWith<IllegalArgumentException> {
            viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location1", "Test Address2")
        }
    }

    @Test
    fun testRemoveFavoriteLocation() = runTest {
        val viewModel = FavoritesScreenViewModel()
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")
        viewModel.addFavoriteLocation(LatLng(3.0, 4.0), "Test Location2", "Test Address2")
        viewModel.addFavoriteLocation(LatLng(5.0, 6.0), "Test Location3", "Test Address3")
        if (!((viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1")) &&
                    (viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location2")) &&
                    (viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location3")))){
            fail("Some or all of the favorite locations were not added")
        }
        viewModel.removeFavoriteLocation("Test Location1")
        assert(!(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location1")))

        viewModel.removeFavoriteLocation("Test Location2")
        assert(!(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location2")))

        viewModel.removeFavoriteLocation("Test Location3")
        assert(!(viewModel.uiState.value.favoritesLocationMap.containsKey("Test Location3")))
    }

    @Test
    fun testRemoveNonExistentFavoriteLocation() = runTest {
        val viewModel = FavoritesScreenViewModel()
        assertFailsWith<IllegalArgumentException> {
            viewModel.removeFavoriteLocation("NonExistent Location")
        }
    }

    @Test
    fun testChangeFavoriteIcon() = runTest {
        val viewModel = FavoritesScreenViewModel()
        viewModel.addFavoriteLocation(LatLng(1.0, 2.0), "Test Location1", "Test Address1")
        val newIcon = Icons.Default.Menu
        viewModel.changeFavoriteIcon("Test Location1", newIcon)
        assert(viewModel.uiState.value.favoritesLocationMap["Test Location1"]?.icon == newIcon)

    }
}