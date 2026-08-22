package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

internal class AceWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsVehicleApproachPreferences>,
) : AceWindowsVehicleApproachPreferencesRepository {
    override fun observeThresholdMeters(): Flow<Double> = dataStore.observeProperty { it.thresholdMeters }

    override suspend fun saveThresholdMeters(meters: Double) {
        dataStore.saveProperty(meters) { prefs, value -> prefs.copy(thresholdMeters = value) }
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
