package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsTyreTemperaturePreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

// saveLowWarningPhases が明示的な true/false を書き込む対象フェーズの全体集合。
// デフォルト有効状態は ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase に一元化しているため、
// ここでは永続化対象のキー空間の定義にとどめる。
private val allLowWarningPhases: Set<SessionPhase> = setOf(
    SessionPhase.GARAGE,
    SessionPhase.WARM_UP,
    SessionPhase.GRID_WALK,
    SessionPhase.FORMATION,
)

internal class LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreTemperaturePreferences>,
) : LmuWindowsTyreTemperaturePreferencesRepository {

    override fun observeHighThresholdCelsius(): Flow<Int> =
        dataStore.data.map { it.highThresholdCelsius }

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        dataStore.updateData { it.copy(highThresholdCelsius = celsius) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key.value to enabled)) }
    }

    override fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.lowWarningPhases
                .mapNotNull { (raw, enabled) ->
                    SessionPhase.fromRaw(raw).takeIf { it != SessionPhase.UNKNOWN }?.let { it to enabled }
                }
                .toMap()
        }

    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) {
        val explicitPhases = allLowWarningPhases.associate { phase -> phase.rawValue to (phase in phases) }
        dataStore.updateData { it.copy(lowWarningPhases = explicitPhases) }
    }
}
