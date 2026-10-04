package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsVehicleDamagePreferencesRepository {
    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    fun observeOverheatReadoutText(): Flow<String>

    suspend fun saveOverheatReadoutText(text: String)

    fun observePartDetachedReadoutText(): Flow<String>

    suspend fun savePartDetachedReadoutText(text: String)

    fun observeTyreDetachedReadoutText(): Flow<String>

    suspend fun saveTyreDetachedReadoutText(text: String)
}
