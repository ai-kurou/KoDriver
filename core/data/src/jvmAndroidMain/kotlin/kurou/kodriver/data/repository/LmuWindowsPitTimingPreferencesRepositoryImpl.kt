package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

internal class LmuWindowsPitTimingPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsPitTimingPreferences>,
) : LmuWindowsPitTimingPreferencesRepository {
    override fun observeVirtualEnergyLaps(): Flow<Int> = dataStore.observeProperty { it.virtualEnergyLaps }

    override suspend fun saveVirtualEnergyLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(virtualEnergyLaps = value) }
    }

    override fun observeTyreWearLaps(): Flow<Int> = dataStore.observeProperty { it.tyreWearLaps }

    override suspend fun saveTyreWearLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(tyreWearLaps = value) }
    }
}
