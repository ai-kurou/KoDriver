package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

internal class ManualReadoutPlayButtonLayoutPreferencesRepositoryImpl(
    private val dataStore: DataStore<ManualReadoutPlayButtonLayoutPreferences>,
) : ManualReadoutPlayButtonLayoutEnabledPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { ManualReadoutPlayButtonLayoutPreferences() }
    }

    override fun observeManualReadoutPlayButtonLayoutEnabled(): Flow<Boolean> = dataStore.observeProperty { it.enabled }

    override suspend fun saveManualReadoutPlayButtonLayoutEnabled(enabled: Boolean) {
        dataStore.saveProperty(enabled) { prefs, value -> prefs.copy(enabled = value) }
    }
}
