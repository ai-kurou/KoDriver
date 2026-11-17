package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

public interface LmuWindowsVehicleDamagePreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    public fun observeOverheatReadoutText(): Flow<String>

    public suspend fun saveOverheatReadoutText(text: String)

    public fun observePartDetachedReadoutText(): Flow<String>

    public suspend fun savePartDetachedReadoutText(text: String)

    public fun observeTyreDetachedReadoutText(): Flow<String>

    public suspend fun saveTyreDetachedReadoutText(text: String)
}
