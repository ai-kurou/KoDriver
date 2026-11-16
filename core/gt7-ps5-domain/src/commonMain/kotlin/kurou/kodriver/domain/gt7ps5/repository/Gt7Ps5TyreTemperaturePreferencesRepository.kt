package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey

public interface Gt7Ps5TyreTemperaturePreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeHighThresholdCelsius(): Flow<Celsius>

    public suspend fun saveHighThresholdCelsius(celsius: Celsius)

    public fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    public suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )

    public fun observeOverheatReadoutText(): Flow<String>

    public suspend fun saveOverheatReadoutText(text: String)
}
