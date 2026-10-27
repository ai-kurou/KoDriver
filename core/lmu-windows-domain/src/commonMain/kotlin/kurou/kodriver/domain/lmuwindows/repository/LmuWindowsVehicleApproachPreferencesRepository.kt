package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsVehicleApproachPreferencesRepository {
    suspend fun resetToDefault()

    fun observeSkipFirstLap(): Flow<Boolean>

    suspend fun saveSkipFirstLap(skip: Boolean)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
