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
            prefs.sets.map { ManualReadoutSet(id = it.id, itemKeys = it.itemKeys, itemTexts = it.itemTexts) }
        }

    override suspend fun addSet(set: ManualReadoutSet) {
        dataStore.updateData { prefs ->
            prefs.copy(sets = prefs.sets + ManualReadoutSetPreference(set.id, set.itemKeys, set.itemTexts))
        }
    }

    override suspend fun updateSet(set: ManualReadoutSet) {
        dataStore.updateData { prefs ->
            prefs.copy(
                sets =
                    prefs.sets.map {
                        if (it.id == set.id) ManualReadoutSetPreference(set.id, set.itemKeys, set.itemTexts) else it
                    },
            )
        }
    }

    override suspend fun deleteSet(id: String) {
        dataStore.updateData { prefs -> prefs.copy(sets = prefs.sets.filterNot { it.id == id }) }
    }

    override suspend fun saveSets(sets: List<ManualReadoutSet>) {
        dataStore.saveProperty(sets) { prefs, value ->
            prefs.copy(sets = value.map { ManualReadoutSetPreference(it.id, it.itemKeys, it.itemTexts) })
        }
    }
}
