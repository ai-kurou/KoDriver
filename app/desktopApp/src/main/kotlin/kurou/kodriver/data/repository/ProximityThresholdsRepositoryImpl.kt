package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.ProximityThresholdsPreferences
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

internal class ProximityThresholdsRepositoryImpl(
    private val dataStore: DataStore<ProximityThresholdsPreferences>,
) : ProximityThresholdsRepository {

    override fun observeLongitudinalThresholdMeters(): Flow<Double> =
        dataStore.data.map { it.longitudinalThresholdMeters }

    override fun observeLateralThresholdMeters(): Flow<Double> =
        dataStore.data.map { it.lateralThresholdMeters }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        dataStore.updateData { it.copy(longitudinalThresholdMeters = meters) }
    }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        dataStore.updateData { it.copy(lateralThresholdMeters = meters) }
    }
}
