package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsVehicleApproachThresholdsPreferences
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository

internal class LmuWindowsVehicleApproachThresholdsPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachThresholdsPreferences>,
) : LmuWindowsVehicleApproachThresholdsPreferencesRepository {
    override fun observeLongitudinalThresholdMeters(): Flow<Double> =
        dataStore.data.map { it.longitudinalThresholdMeters }

    override fun observeLateralThresholdMeters(): Flow<Double> = dataStore.data.map { it.lateralThresholdMeters }

    override fun observeSustainedApproachDurationSeconds(): Flow<Int> =
        dataStore.data.map { it.sustainedApproachDurationSeconds }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        dataStore.updateData { it.copy(longitudinalThresholdMeters = meters) }
    }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        dataStore.updateData { it.copy(lateralThresholdMeters = meters) }
    }

    override suspend fun saveSustainedApproachDurationSeconds(seconds: Int) {
        dataStore.updateData { it.copy(sustainedApproachDurationSeconds = seconds) }
    }
}
