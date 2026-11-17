package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.ReadoutItemKey

public interface LmuWindowsTyreTemperaturePreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    public fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>>

    public suspend fun saveLowWarningPhases(phases: Set<SessionPhase>)
}
