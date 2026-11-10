package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository

internal class MainWindowBoundsPreferencesRepositoryImpl(
    private val dataStore: DataStore<MainWindowBoundsPreferences>,
) : MainWindowBoundsPreferencesRepository {
    override fun observeMainWindowBounds(): Flow<MainWindowBounds> =
        dataStore.observeProperty {
            MainWindowBounds(x = it.x, y = it.y, width = it.width, height = it.height)
        }

    override suspend fun saveMainWindowBounds(bounds: MainWindowBounds) {
        dataStore.saveProperty(bounds) { prefs, value ->
            prefs.copy(x = value.x, y = value.y, width = value.width, height = value.height)
        }
    }
}
