package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase

interface LmuWindowsTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Int>

    suspend fun saveHighThresholdCelsius(celsius: Int)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>>

    suspend fun saveLowWarningPhases(phases: Set<SessionPhase>)
}
