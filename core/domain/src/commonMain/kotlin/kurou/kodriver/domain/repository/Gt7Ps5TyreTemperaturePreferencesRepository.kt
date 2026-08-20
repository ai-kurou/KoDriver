package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey

interface Gt7Ps5TyreTemperaturePreferencesRepository {
    fun observeHighThresholdCelsius(): Flow<Celsius>

    suspend fun saveHighThresholdCelsius(celsius: Celsius)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
