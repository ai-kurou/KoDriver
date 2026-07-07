package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey

interface LmuWindowsTyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Int>
    suspend fun saveHighThresholdCelsius(celsius: Int)
    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>
    suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean)
}
