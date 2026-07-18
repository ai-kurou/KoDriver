package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyLapsPreferences
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

internal class LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsRemainingVirtualEnergyLapsPreferences>,
) : LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository {

    override fun observeRemainingVirtualEnergyLaps(): Flow<Int> =
        dataStore.data.map { it.remainingVirtualEnergyLaps }

    override suspend fun saveRemainingVirtualEnergyLaps(laps: Int) {
        dataStore.updateData { it.copy(remainingVirtualEnergyLaps = laps) }
    }
}
