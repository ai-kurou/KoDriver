package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreTemperaturePreferences>,
) : LmuWindowsTyreTemperaturePreferencesRepository {

    override fun observeHighThresholdCelsius(): Flow<Int> =
        dataStore.data.map { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.updateData { it.copy(highThresholdCelsius = celsius) }
    }
}
