package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

internal class ManualReadoutSetPreferencesRepositoryImpl(
    private val dataStore: DataStore<ManualReadoutSetPreferences>,
) : ManualReadoutSetPreferencesRepository {
    override fun observeSets(): Flow<List<ManualReadoutSet>> =
        dataStore.observeProperty { prefs ->
            prefs.sets.map { ManualReadoutSet(id = it.id, itemKeys = it.itemKeys) }
        }

    override suspend fun saveSets(sets: List<ManualReadoutSet>) {
        dataStore.saveProperty(sets) { prefs, value ->
            prefs.copy(sets = value.map { ManualReadoutSetPreference(id = it.id, itemKeys = it.itemKeys) })
        }
    }
}
