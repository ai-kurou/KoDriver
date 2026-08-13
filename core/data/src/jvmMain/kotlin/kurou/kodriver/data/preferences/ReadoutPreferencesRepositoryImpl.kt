package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class ReadoutPreferencesRepositoryImpl(
    private val dataStore: DataStore<ReadoutPreferences>,
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.simulatorStates
                .getOrElse(simulator) { SimulatorReadoutState() }
                .enabledStates
                .toReadoutItemKeyMap()
        }

    override suspend fun saveReadoutEnabledState(
        simulator: String,
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        dataStore.saveProperty(enabled) { prefs, value ->
            val current = prefs.simulatorStates[simulator] ?: SimulatorReadoutState()
            val newState = current.copy(enabledStates = current.enabledStates + (key.value to value))
            prefs.copy(simulatorStates = prefs.simulatorStates + (simulator to newState))
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        dataStore.observeProperty { prefs ->
            prefs.simulatorStates
                .getOrElse(simulator) { SimulatorReadoutState() }
                .itemOrder
                .mapNotNull(ReadoutItemKey::fromValue)
        }

    override suspend fun saveReadoutOrder(
        simulator: String,
        order: List<ReadoutItemKey>,
    ) {
        dataStore.saveProperty(order) { prefs, value ->
            val current = prefs.simulatorStates[simulator] ?: SimulatorReadoutState()
            val newState = current.copy(itemOrder = value.map { it.value })
            prefs.copy(simulatorStates = prefs.simulatorStates + (simulator to newState))
        }
    }

    private fun Map<String, Boolean>.toReadoutItemKeyMap(): Map<ReadoutItemKey, Boolean> =
        mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }.toMap()
}
