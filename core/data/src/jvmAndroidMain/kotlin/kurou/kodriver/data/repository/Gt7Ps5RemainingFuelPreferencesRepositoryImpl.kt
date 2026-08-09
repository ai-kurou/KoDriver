package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelPreferences
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository

internal class Gt7Ps5RemainingFuelPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5RemainingFuelPreferences>,
) : Gt7Ps5RemainingFuelPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }
}
