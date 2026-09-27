package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository

internal class OverlayVisiblePreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayVisiblePreferences>,
) : OverlayVisiblePreferencesRepository {
    override fun observeOverlayVisible(): Flow<Boolean> = dataStore.observeProperty { it.visible }

    override suspend fun saveOverlayVisible(visible: Boolean) {
        dataStore.saveProperty(visible) { prefs, value -> prefs.copy(visible = value) }
    }
}
