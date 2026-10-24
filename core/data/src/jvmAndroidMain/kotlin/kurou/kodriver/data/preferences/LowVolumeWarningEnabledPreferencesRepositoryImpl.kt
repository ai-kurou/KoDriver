package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

internal class LowVolumeWarningEnabledPreferencesRepositoryImpl(
    private val dataStore: DataStore<LowVolumeWarningEnabledPreferences>,
) : LowVolumeWarningEnabledPreferencesRepository {
    override fun observeLowVolumeWarningEnabled(): Flow<Boolean> = dataStore.observeProperty { it.enabled }

    override suspend fun saveLowVolumeWarningEnabled(enabled: Boolean) {
        dataStore.saveProperty(enabled) { prefs, value -> prefs.copy(enabled = value) }
    }
}
