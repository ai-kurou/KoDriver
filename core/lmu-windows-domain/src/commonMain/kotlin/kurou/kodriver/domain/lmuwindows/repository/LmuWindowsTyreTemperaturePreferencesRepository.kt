package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsTyreTemperaturePreferencesRepository {
    suspend fun resetToDefault()

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>>

    suspend fun saveLowWarningPhases(phases: Set<SessionPhase>)
}
