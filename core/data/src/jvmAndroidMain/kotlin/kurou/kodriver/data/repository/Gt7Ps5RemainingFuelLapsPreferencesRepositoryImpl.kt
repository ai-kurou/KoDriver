package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

internal class Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5RemainingFuelLapsPreferences>,
) : Gt7Ps5RemainingFuelLapsPreferencesRepository {

    override fun observeRemainingFuelLaps(): Flow<Int> =
        dataStore.data.map { it.remainingFuelLaps }

    override suspend fun saveRemainingFuelLaps(laps: Int) {
        dataStore.updateData { it.copy(remainingFuelLaps = laps) }
    }
}
