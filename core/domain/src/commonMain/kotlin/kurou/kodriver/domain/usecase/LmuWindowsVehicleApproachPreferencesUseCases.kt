package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

class LmuWindowsVehicleApproachPreferencesUseCases(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
) {
    fun observeSkipFirstLap(): Flow<Boolean> = repository.observeSkipFirstLap()

    suspend fun saveSkipFirstLap(skip: Boolean) = repository.saveSkipFirstLap(skip)

    fun observeStartLeftReadoutText(): Flow<String> = repository.observeStartLeftReadoutText()

    suspend fun saveStartLeftReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(repository)(text)

    fun observeStartRightReadoutText(): Flow<String> = repository.observeStartRightReadoutText()

    suspend fun saveStartRightReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(repository)(text)

    fun observeSustainedLeftReadoutText(): Flow<String> = repository.observeSustainedLeftReadoutText()

    suspend fun saveSustainedLeftReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(repository)(text)

    fun observeSustainedRightReadoutText(): Flow<String> = repository.observeSustainedRightReadoutText()

    suspend fun saveSustainedRightReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(repository)(text)
}
