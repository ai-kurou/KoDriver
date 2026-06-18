package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

class VehicleApproachPreferencesUseCases(
    private val repository: VehicleApproachPreferencesRepository,
) {
    fun observeSkipFirstLap(): Flow<Boolean> = repository.observeSkipFirstLap()

    suspend fun saveSkipFirstLap(skip: Boolean) = repository.saveSkipFirstLap(skip)

    fun observeStartReadoutEnabled(): Flow<Boolean> = repository.observeStartReadoutEnabled()

    suspend fun saveStartReadoutEnabled(enabled: Boolean) = repository.saveStartReadoutEnabled(enabled)

    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = repository.observeStartReadoutType()

    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) = repository.saveStartReadoutType(type)
}
