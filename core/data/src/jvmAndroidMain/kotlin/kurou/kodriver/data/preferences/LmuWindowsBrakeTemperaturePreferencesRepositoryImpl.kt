package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperaturePreferencesRepository

internal class LmuWindowsBrakeTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsBrakeTemperaturePreferences>,
) : LmuWindowsBrakeTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Int> = dataStore.observeProperty { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.saveProperty(celsius) { prefs, value -> prefs.copy(highThresholdCelsius = value) }
    }
}
