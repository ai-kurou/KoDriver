package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

internal class LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachThresholdsPreferences>,
) : LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    override fun observeLongitudinalThresholdMeters(): Flow<Double> =
        dataStore.observeProperty { it.longitudinalThresholdMeters }

    override fun observeLateralThresholdMeters(): Flow<Double> = dataStore.observeProperty { it.lateralThresholdMeters }

    override fun observeSustainedApproachDurationSeconds(): Flow<Int> =
        dataStore.observeProperty { it.sustainedApproachDurationSeconds }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        dataStore.saveProperty(meters) { prefs, value -> prefs.copy(longitudinalThresholdMeters = value) }
    }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        dataStore.saveProperty(meters) { prefs, value -> prefs.copy(lateralThresholdMeters = value) }
    }

    override suspend fun saveSustainedApproachDurationSeconds(seconds: Int) {
        dataStore.saveProperty(seconds) { prefs, value -> prefs.copy(sustainedApproachDurationSeconds = value) }
    }
}
