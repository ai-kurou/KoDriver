package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface LmuWindowsVehicleApproachPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeSkipFirstLap(): Flow<Boolean>

    public suspend fun saveSkipFirstLap(skip: Boolean)

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
