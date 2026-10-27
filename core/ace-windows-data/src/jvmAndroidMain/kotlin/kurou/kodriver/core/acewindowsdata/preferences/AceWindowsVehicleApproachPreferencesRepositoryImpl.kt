package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.ReadoutItemKey

internal class AceWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsVehicleApproachPreferences>,
) : AceWindowsVehicleApproachPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { AceWindowsVehicleApproachPreferences() }
    }

    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.readoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(readoutText = value) }
    }

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
