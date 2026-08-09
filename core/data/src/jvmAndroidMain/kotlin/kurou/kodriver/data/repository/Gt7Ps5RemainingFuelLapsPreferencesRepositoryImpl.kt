package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

internal class Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5RemainingFuelLapsPreferences>,
) : Gt7Ps5RemainingFuelLapsPreferencesRepository {
    override fun observeRemainingFuelLaps(): Flow<Int> = dataStore.observeProperty { it.remainingFuelLaps }

    override suspend fun saveRemainingFuelLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(remainingFuelLaps = value) }
    }
}
