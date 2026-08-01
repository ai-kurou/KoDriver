package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository

internal class LmuWindowsPitTimingPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsPitTimingPreferences>,
) : LmuWindowsPitTimingPreferencesRepository {
    override fun observeVirtualEnergyLaps(): Flow<Int> =
        dataStore.data.map { it.virtualEnergyLaps }

    override suspend fun saveVirtualEnergyLaps(laps: Int) {
        dataStore.updateData { it.copy(virtualEnergyLaps = laps) }
    }

    override fun observeTyreWearLaps(): Flow<Int> =
        dataStore.data.map { it.tyreWearLaps }

    override suspend fun saveTyreWearLaps(laps: Int) {
        dataStore.updateData { it.copy(tyreWearLaps = laps) }
    }
}
