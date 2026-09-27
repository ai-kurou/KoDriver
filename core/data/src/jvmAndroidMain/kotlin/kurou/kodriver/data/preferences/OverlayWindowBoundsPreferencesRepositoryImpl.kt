package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository

internal class OverlayWindowBoundsPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayWindowBoundsPreferences>,
) : OverlayWindowBoundsPreferencesRepository {
    override fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds> =
        dataStore.observeProperty {
            OverlayWindowBounds(x = it.x, y = it.y, width = it.width, height = it.height)
        }

    override suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds) {
        dataStore.saveProperty(bounds) { prefs, value ->
            prefs.copy(x = value.x, y = value.y, width = value.width, height = value.height)
        }
    }
}
