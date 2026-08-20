package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

internal class AceWindowsTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsTyreTemperaturePreferences>,
) : AceWindowsTyreTemperaturePreferencesRepository {
    override fun observeHighThresholdCelsius(): Flow<Celsius> =
        dataStore.observeProperty { it.highThresholdCelsius }.map { Celsius(it) }

    override suspend fun saveHighThresholdCelsius(celsius: Celsius) {
        dataStore.saveProperty(celsius.value) { prefs, value -> prefs.copy(highThresholdCelsius = value) }
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
