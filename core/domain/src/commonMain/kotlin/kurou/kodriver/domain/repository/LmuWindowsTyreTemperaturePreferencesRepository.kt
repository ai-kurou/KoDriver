package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase

interface LmuWindowsTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Celsius>

    suspend fun saveHighThresholdCelsius(celsius: Celsius)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>>

    suspend fun saveLowWarningPhases(phases: Set<SessionPhase>)
}
