package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

internal class AceWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsVehicleApproachPreferences>,
) : AceWindowsVehicleApproachPreferencesRepository {
    override fun observeLongitudinalThresholdMeters(): Flow<Double> =
        dataStore.observeProperty { it.longitudinalThresholdMeters }

    override suspend fun saveLongitudinalThresholdMeters(meters: Double) {
        dataStore.saveProperty(meters) { prefs, value -> prefs.copy(longitudinalThresholdMeters = value) }
    }

    override fun observeLateralThresholdMeters(): Flow<Double> = dataStore.observeProperty { it.lateralThresholdMeters }

    override suspend fun saveLateralThresholdMeters(meters: Double) {
        dataStore.saveProperty(meters) { prefs, value -> prefs.copy(lateralThresholdMeters = value) }
    }

    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> =
        dataStore.observeProperty { VehicleApproachStartReadoutType.fromId(it.startReadoutType) }

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(startReadoutType = value) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        dataStore.saveProperty(enabled) { prefs, value ->
            prefs.copy(
                enabledStates =
                    prefs.enabledStates + (key.value to value),
            )
        }
    }
}
