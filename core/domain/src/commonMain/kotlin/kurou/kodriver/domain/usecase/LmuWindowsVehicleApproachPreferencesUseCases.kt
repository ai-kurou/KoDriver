package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class LmuWindowsVehicleApproachPreferencesUseCases(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    fun observeSkipFirstLap(): Flow<Boolean> = repository.observeSkipFirstLap()

    suspend fun saveSkipFirstLap(skip: Boolean) = repository.saveSkipFirstLap(skip)

    fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = repository.observeStartReadoutType()

    suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) = repository.saveStartReadoutType(type)

    fun observeSustainedReadoutType(): Flow<VehicleApproachSustainedReadoutType> =
        repository.observeSustainedReadoutType()

    suspend fun saveSustainedReadoutType(type: VehicleApproachSustainedReadoutType) =
        repository.saveSustainedReadoutType(type)
}
