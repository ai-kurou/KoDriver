package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.Gt7Ps5TyreTemperaturePreferences
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

internal class Gt7Ps5TyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5TyreTemperaturePreferences>,
) : Gt7Ps5TyreTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Int> = dataStore.observeProperty { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.saveProperty(celsius) { prefs, value -> prefs.copy(highThresholdCelsius = value) }
    }
}
