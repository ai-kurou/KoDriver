package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

@Suppress("TooManyFunctions")
interface LmuWindowsPitTimingPreferencesRepository {
    fun observeVirtualEnergyLaps(): Flow<Int>

    suspend fun saveVirtualEnergyLaps(laps: Int)

    fun observeTyreWearLaps(): Flow<Int>

    suspend fun saveTyreWearLaps(laps: Int)

    fun observeVirtualEnergyReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyReadoutText(text: String)

    fun observeVirtualEnergyImminentReadoutText(): Flow<String>

    suspend fun saveVirtualEnergyImminentReadoutText(text: String)

    fun observeTyreWearReadoutText(): Flow<String>

    suspend fun saveTyreWearReadoutText(text: String)

    fun observeTyreWearImminentReadoutText(): Flow<String>

    suspend fun saveTyreWearImminentReadoutText(text: String)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
