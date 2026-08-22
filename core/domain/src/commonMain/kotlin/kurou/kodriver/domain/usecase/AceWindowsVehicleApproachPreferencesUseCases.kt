package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository

class AceWindowsVehicleApproachPreferencesUseCases(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = repository.observeStartReadoutType()

    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) = repository.saveStartReadoutType(type)
}
