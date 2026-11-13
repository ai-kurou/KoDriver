package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository

internal class ManualReadoutSetPreferencesRepositoryImpl(
    private val dataStore: DataStore<ManualReadoutSetPreferences>,
    private val migrateLegacySet: () -> (ManualReadoutSet) -> ManualReadoutSet = { { it } },
) : ManualReadoutSetPreferencesRepository {
    override fun observeSets(): Flow<List<ManualReadoutSet>> =
        dataStore.observeProperty { prefs ->
            val migrate = migrateLegacySet()
            prefs.sets.map { migrate(it.toDomain()) }
        }

    override suspend fun addSet(set: ManualReadoutSet) {
        dataStore.updateData { prefs ->
            val numberedSet =
                if (set.number == 0) {
                    set.copy(number = maxOf(prefs.sets.size, prefs.sets.maxOfOrNull { it.number } ?: 0) + 1)
                } else {
                    set
                }
            prefs.copy(sets = prefs.sets + numberedSet.toPreference())
        }
    }

    override suspend fun updateSet(set: ManualReadoutSet) {
        dataStore.updateData { prefs ->
            prefs.copy(
                sets =
                    prefs.sets.map {
                        if (it.id == set.id) set.toPreference() else it
                    },
            )
        }
    }

    override suspend fun deleteSet(id: String) {
        dataStore.updateData { prefs -> prefs.copy(sets = prefs.sets.filterNot { it.id == id }) }
    }

    override suspend fun resetToDefault() {
        dataStore.updateData { ManualReadoutSetPreferences() }
    }

    override suspend fun saveSets(sets: List<ManualReadoutSet>) {
        dataStore.saveProperty(sets) { prefs, value ->
            prefs.copy(sets = value.map { it.toPreference() })
        }
    }
}

private fun ManualReadoutSet.toPreference() =
    ManualReadoutSetPreference(id, itemKeys, itemTexts, hotkeyVirtualKeyCode, hotkeyModifiers.value, name, number)

private fun ManualReadoutSetPreference.toDomain() =
    ManualReadoutSet(
        id = id,
        name = name,
        number = number,
        itemKeys = itemKeys,
        itemTexts = itemTexts,
        hotkeyVirtualKeyCode = hotkeyVirtualKeyCode,
        hotkeyModifiers = ManualReadoutHotkeyModifiers.fromValue(hotkeyModifiers),
    )
