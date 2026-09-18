package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

internal class OverlayBackgroundOpacityPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverlayBackgroundOpacityPreferences>,
) : OverlayBackgroundOpacityPreferencesRepository {
    override fun observeOverlayBackgroundOpacity(): Flow<Int> = dataStore.observeProperty { it.opacity }

    override suspend fun saveOverlayBackgroundOpacity(opacity: Int) {
        dataStore.saveProperty(opacity) { prefs, value -> prefs.copy(opacity = value) }
    }
}
